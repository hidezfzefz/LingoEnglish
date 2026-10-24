#!/usr/bin/env bash
# Deploy the live-call schema, LiveKit secrets, the token function, and the
# public auth callback page.
#
# Requires:
#   SUPABASE_ACCESS_TOKEN  (Supabase personal access token)
#   SUPABASE_PROJECT_REF
#   LIVEKIT_URL
#   LIVEKIT_API_KEY
#   LIVEKIT_API_SECRET
#
# Set SKIP_DB_PUSH=1 if calls_schema.sql was already run in the SQL editor.
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="$repo_root/supabase/.env.local"
if [[ -f "$env_file" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$env_file"
  set +a
fi

if [[ -z "${SUPABASE_PROJECT_REF:-}" && "${SUPABASE_URL:-}" =~ ^https://([^.]+)\.supabase\.co/?$ ]]; then
  SUPABASE_PROJECT_REF="${BASH_REMATCH[1]}"
fi

: "${SUPABASE_ACCESS_TOKEN:?Set SUPABASE_ACCESS_TOKEN (or add it to supabase/.env.local)}"
: "${SUPABASE_PROJECT_REF:?Set SUPABASE_PROJECT_REF (or add it to supabase/.env.local)}"
: "${LIVEKIT_URL:?Set LIVEKIT_URL (wss://...)}"
: "${LIVEKIT_API_KEY:?Set LIVEKIT_API_KEY}"
: "${LIVEKIT_API_SECRET:?Set LIVEKIT_API_SECRET}"

cd "$repo_root/supabase"

link_args=(--project-ref "$SUPABASE_PROJECT_REF" --yes)
db_args=(--linked --yes)
if [[ -n "${SUPABASE_DB_PASSWORD:-}" ]]; then
  link_args+=(--password "$SUPABASE_DB_PASSWORD")
  db_args+=(--password "$SUPABASE_DB_PASSWORD")
fi

echo "Linking Supabase project $SUPABASE_PROJECT_REF..."
npx supabase link "${link_args[@]}"

if [[ "${SKIP_DB_PUSH:-0}" != "1" ]]; then
  echo "Applying database migrations..."
  npx supabase db push "${db_args[@]}"
else
  echo "Skipping db push (SKIP_DB_PUSH=1)."
fi

echo "Setting Edge Function secrets..."
npx supabase secrets set --project-ref "$SUPABASE_PROJECT_REF" --yes \
  "LIVEKIT_URL=$LIVEKIT_URL" \
  "LIVEKIT_API_KEY=$LIVEKIT_API_KEY" \
  "LIVEKIT_API_SECRET=$LIVEKIT_API_SECRET"

echo "Deploying livekit-token..."
npx supabase functions deploy livekit-token \
  --project-ref "$SUPABASE_PROJECT_REF" --yes

echo "Deploying public auth callback page..."
npx supabase functions deploy auth-callback \
  --project-ref "$SUPABASE_PROJECT_REF" --yes --no-verify-jwt

echo "Done. Configure the app with scripts/configure_live.sh."
