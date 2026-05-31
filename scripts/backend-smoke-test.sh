#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
LOGIN_USER="${LOGIN_USER:-lukimia}"
LOGIN_PASSWORD="${LOGIN_PASSWORD:-lukimia}"

echo "== Kawan Kasir backend smoke test =="
echo "Base URL: ${BASE_URL}"

login_payload=$(cat <<JSON
{"usernameOrEmail":"${LOGIN_USER}","password":"${LOGIN_PASSWORD}"}
JSON
)

login_response=$(curl -fsS -X POST "${BASE_URL}/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d "${login_payload}")

token=$(python3 - <<PY
import json
payload = json.loads('''${login_response}''')
print(payload.get('data', {}).get('accessToken', ''))
PY
)

if [[ -z "${token}" ]]; then
  echo "Login gagal: accessToken kosong" >&2
  echo "${login_response}" >&2
  exit 1
fi

echo "✓ Login developer berhasil"

curl -fsS "${BASE_URL}/api/auth/me" -H "Authorization: Bearer ${token}" >/dev/null
echo "✓ /api/auth/me berhasil"

curl -fsS "${BASE_URL}/api/reports/dashboard" -H "Authorization: Bearer ${token}" >/dev/null
echo "✓ /api/reports/dashboard berhasil"

curl -fsS "${BASE_URL}/api/products" -H "Authorization: Bearer ${token}" >/dev/null
echo "✓ /api/products berhasil"

unique="smoke$(date +%s)"
register_payload=$(cat <<JSON
{"storeName":"Smoke Test Store","ownerName":"Smoke Owner","email":"${unique}@kawankasir.local","username":"${unique}","phone":"080000000001","password":"password123"}
JSON
)

curl -fsS -X POST "${BASE_URL}/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "${register_payload}" >/dev/null
echo "✓ /api/auth/register berhasil"

echo "Semua smoke test backend berhasil."
