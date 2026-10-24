#!/usr/bin/env bash
# Point the Android build at a Supabase project without losing sdk.dir.
#
# Values may be passed as arguments, exported, or left in supabase/.env.local:
#   SUPABASE_URL / SUPABASE_ANON_KEY
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="$repo_root/supabase/.env.local"

env_value() {
  local key="$1" line
  [[ -f "$env_file" ]] || return 0
  line="$(grep -E "^[[:space:]]*${key}=" "$env_file" | tail -n 1 || true)"
  [[ -n "$line" ]] || return 0
  printf '%s' "${line#*=}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' \
    -e 's/^"//' -e 's/"$//' -e "s/^'//" -e "s/'$//"
}

url="${1:-${LINGO_SUPABASE_URL:-${SUPABASE_URL:-}}}"
key="${2:-${LINGO_SUPABASE_ANON_KEY:-${SUPABASE_ANON_KEY:-}}}"
[[ -n "$url" ]] || url="$(env_value SUPABASE_URL)"
[[ -n "$key" ]] || key="$(env_value SUPABASE_ANON_KEY)"

url="${url%/}"

if [[ ! "$url" =~ ^https://[^/]+$ ]]; then
  echo "Usage: $0 https://YOUR-PROJECT.supabase.co YOUR_ANON_KEY" >&2
  echo "Or fill SUPABASE_URL and SUPABASE_ANON_KEY in supabase/.env.local." >&2
  echo "The URL must be the project base URL, without a trailing slash." >&2
  exit 1
fi
if [[ -z "$key" ]]; then
  echo "The Supabase anon/publishable key is required." >&2
  echo "Add SUPABASE_ANON_KEY to supabase/.env.local." >&2
  exit 1
fi

python3 - "$repo_root/local.properties" "$url" "$key" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
url = sys.argv[2].strip()
key = sys.argv[3].strip()
lines = path.read_text().splitlines() if path.exists() else []
out = [
    line for line in lines
    if not line.startswith("LINGO_SUPABASE_URL=")
    and not line.startswith("LINGO_SUPABASE_ANON_KEY=")
]
out.append(f"LINGO_SUPABASE_URL={url}")
out.append(f"LINGO_SUPABASE_ANON_KEY={key}")
path.write_text("\n".join(out).rstrip() + "\n")
PY

echo "Configured local.properties for $url"
echo "The anon key is present ($(printf '%s' "$key" | wc -c) characters)."
echo "Build with: ./gradlew :app:assembleRelease --no-daemon --max-workers=2"
