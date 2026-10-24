#!/usr/bin/env bash
# Configure Google SMTP for Supabase auth emails.
#
# Google Workspace is recommended for production. It needs a verified custom
# domain and a mailbox/alias for the sender. The same script also accepts a free
# Gmail address for early testing, but free Gmail has low daily sending limits.
#
# Requirements:
#   - A Google Workspace (or Gmail) account with 2-Step Verification enabled.
#   - A 16-character Google App Password for that account.
#
# Interactive:
#   ./supabase/setup_google_smtp.sh
#
# Non-interactive (for automation; keep the secret file mode 600):
#   LINGO_GOOGLE_SENDER=auth@yourdomain.com \
#   LINGO_GOOGLE_APP_PASSWORD_FILE=/path/to/google-app-password \
#   ./supabase/setup_google_smtp.sh
#
# The app password is read without echo, written to the mode-600 env file, and
# never printed. Supabase configuration is only changed after Google accepts
# the SMTP login.
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="$repo_root/supabase/.env.local"

if [[ ! -f "$env_file" ]]; then
  if [[ -f "$env_file.example" ]]; then
    cp "$env_file.example" "$env_file"
  else
    echo "Missing $env_file"
    exit 1
  fi
fi
chmod 600 "$env_file"

sender="${LINGO_GOOGLE_SENDER:-}"
if [[ -z "$sender" ]]; then
  echo "Use the full Google Workspace sender address (for example auth@yourdomain.com)."
  read -r -p "Google sender email: " sender
fi
if [[ ! "$sender" =~ ^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$ ]]; then
  echo "That does not look like an email address."
  exit 1
fi
sender_domain="${sender#*@}"
if [[ "$sender_domain" == "gmail.com" || "$sender_domain" == "googlemail.com" ]]; then
  echo "Note: $sender is a free Gmail address. Google Workspace uses a custom"
  echo "domain and has higher sending limits. Use Workspace for production."
fi

app_password=""
if [[ -n "${LINGO_GOOGLE_APP_PASSWORD_FILE:-}" ]]; then
  password_file="$LINGO_GOOGLE_APP_PASSWORD_FILE"
  if [[ ! -f "$password_file" ]]; then
    echo "App Password file not found: $password_file"
    exit 1
  fi
  chmod 600 "$password_file"
  app_password="$(head -n 1 "$password_file" | tr -d '[:space:]')"
  app_password="${app_password#GOOGLE_APP_PASSWORD=}"
  app_password="${app_password#LINGO_GOOGLE_APP_PASSWORD=}"
else
  echo "Create an App Password at Google Account → Security → App passwords."
  read -r -s -p "Google App Password (16 characters, spaces okay): " app_password
  echo
  app_password="${app_password// /}"
fi
if [[ ${#app_password} -ne 16 ]]; then
  echo "Google App Passwords are 16 characters. Nothing was changed."
  exit 1
fi

export LINGO_GOOGLE_SENDER="$sender"
export LINGO_GOOGLE_APP_PASSWORD="$app_password"
export LINGO_ENV_FILE="$env_file"

python3 - <<'PY'
import os
from pathlib import Path

env_path = Path(os.environ["LINGO_ENV_FILE"])
updates = {
    "SMTP_HOST": "smtp.gmail.com",
    "SMTP_PORT": "587",
    "SMTP_USER": os.environ["LINGO_GOOGLE_SENDER"],
    "SMTP_PASS": os.environ["LINGO_GOOGLE_APP_PASSWORD"],
    "SMTP_ADMIN_EMAIL": os.environ["LINGO_GOOGLE_SENDER"],
    "SMTP_SENDER_NAME": "Lingo",
}
lines = env_path.read_text().splitlines()
seen = set()
out = []
for line in lines:
    stripped = line.strip()
    if stripped and not stripped.startswith("#") and "=" in stripped:
        key = stripped.split("=", 1)[0].strip()
        if key in updates:
            out.append(f"{key}={updates[key]}")
            seen.add(key)
            continue
    out.append(line)
for key, value in updates.items():
    if key not in seen:
        out.append(f"{key}={value}")
env_path.write_text("\n".join(out) + "\n")
PY

python3 - <<'PY'
import os, smtplib, ssl
sender = os.environ["LINGO_GOOGLE_SENDER"]
password = os.environ["LINGO_GOOGLE_APP_PASSWORD"]
try:
    with smtplib.SMTP("smtp.gmail.com", 587, timeout=30) as server:
        server.starttls(context=ssl.create_default_context())
        server.login(sender, password)
except Exception as error:
    print(f"Google SMTP login failed: {type(error).__name__}.")
    print("Nothing was enabled. Check that 2-Step Verification and App Passwords are enabled, then run this again.")
    raise SystemExit(1)
print("Google SMTP login succeeded.")
PY

unset LINGO_GOOGLE_APP_PASSWORD
"$repo_root/supabase/configure_auth.sh"
echo "Google SMTP is now enabled for the auth emails."
echo "Run one real signup and one password reset end to end before inviting learners."
