#!/usr/bin/env bash
# Configure Supabase auth email delivery and enable confirmation.
#
# Fill these values in supabase/.env.local first:
#   SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS,
#   SMTP_ADMIN_EMAIL, SMTP_SENDER_NAME
#
# Then run:
#   ./supabase/configure_auth.sh
#
# The script never prints SMTP credentials. Test one real signup and one
# password reset after running it before inviting learners.
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

: "${SUPABASE_ACCESS_TOKEN:?Set SUPABASE_ACCESS_TOKEN in supabase/.env.local}"
: "${SUPABASE_PROJECT_REF:?Set SUPABASE_PROJECT_REF in supabase/.env.local}"
: "${SUPABASE_URL:?Set SUPABASE_URL in supabase/.env.local}"
: "${SMTP_HOST:?Set SMTP_HOST in supabase/.env.local}"
: "${SMTP_PORT:?Set SMTP_PORT in supabase/.env.local}"
: "${SMTP_USER:?Set SMTP_USER in supabase/.env.local}"
: "${SMTP_PASS:?Set SMTP_PASS in supabase/.env.local}"
: "${SMTP_ADMIN_EMAIL:?Set SMTP_ADMIN_EMAIL in supabase/.env.local}"
SMTP_SENDER_NAME="${SMTP_SENDER_NAME:-Lingo}"
AUTH_SITE_URL="${AUTH_SITE_URL:-${SUPABASE_URL%/}/functions/v1/auth-callback}"
PASSWORD_RESET_URL="lingo://reset-password"

export SUPABASE_ACCESS_TOKEN SUPABASE_PROJECT_REF SUPABASE_URL
export SMTP_HOST SMTP_PORT SMTP_USER SMTP_PASS SMTP_ADMIN_EMAIL SMTP_SENDER_NAME AUTH_SITE_URL PASSWORD_RESET_URL

python3 - <<'PY'
import json, os, urllib.error, urllib.request

ref = os.environ["SUPABASE_PROJECT_REF"]
token = os.environ["SUPABASE_ACCESS_TOKEN"]
site_url = os.environ["AUTH_SITE_URL"].rstrip("/")
reset_url = os.environ["PASSWORD_RESET_URL"].strip()
payload = {
    "smtp_host": os.environ["SMTP_HOST"],
    # The Management API validates this field as a string.
    "smtp_port": os.environ["SMTP_PORT"],
    "smtp_user": os.environ["SMTP_USER"],
    "smtp_pass": os.environ["SMTP_PASS"],
    "smtp_admin_email": os.environ["SMTP_ADMIN_EMAIL"],
    "smtp_sender_name": os.environ["SMTP_SENDER_NAME"],
    "site_url": site_url,
    # The confirmation page plus the Android password-recovery deep link.
    "uri_allow_list": f"{site_url},{reset_url}",
    "mailer_autoconfirm": False,
}
request = urllib.request.Request(
    f"https://api.supabase.com/v1/projects/{ref}/config/auth",
    data=json.dumps(payload).encode(),
    headers={
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
    },
    method="PATCH",
)
try:
    with urllib.request.urlopen(request, timeout=30) as response:
        config = json.load(response)
except urllib.error.HTTPError as error:
    print(f"Auth configuration failed (HTTP {error.code}).")
    raise SystemExit(1)

print("Auth SMTP configured.")
print(f"Site URL: {config.get('site_url')}")
print(f"Password reset redirect: {reset_url}")
print(f"Email confirmation enabled: {not config.get('mailer_autoconfirm')}")
print("Test one signup and one password reset end to end before inviting learners.")
PY
