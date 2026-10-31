# MILI AI — ALIRAJ Admin Panel

Node.js dashboard + API for testing and deployment. Requires Node.js 18+.

## Run locally

```bash
export ADMIN_USER=aliraj
export ADMIN_PASSWORD='use-a-unique-password-at-least-12-characters'
export TOKEN_SECRET="$(openssl rand -hex 32)"
node server.js
```

Open `http://127.0.0.1:8787`. The first-run defaults are not a public password; you must set the environment variables above.

## Admin features
- Secure login with an 8-hour signed token.
- AI personality, model, voice and system prompt configuration.
- Feature flag management.
- Create Premium/Lifetime keys.
- Activation request review with approve/reject.
- Device binding at approval, expiry, and revocation API.
- Dashboard refreshes request queue every 15 seconds; browser notification requires browser permission and dashboard open.

## API
- `GET /api/health`
- `POST /api/login` — `{ "username": "...", "password": "..." }`
- `GET /api/public-config` — public, non-secret config for app clients
- `GET /api/config` — admin token required
- `PUT /api/config` — admin token required
- `GET /api/licenses` — admin token required
- `POST /api/licenses` — admin token required; body `{ "plan": "Premium", "days": 365 }`
- `POST /api/activation-request` — `{ "licenseKey": "...", "deviceId": "...", "appVersion": "..." }`
- `POST /api/license/check` — `{ "licenseKey": "...", "deviceId": "..." }`
- `POST /api/requests/:id/approve` — admin token required
- `POST /api/requests/:id/reject` — admin token required
- `POST /api/licenses/:id/revoke` — admin token required

## Security / production
This starter uses JSON files in `data/` and is intended for local testing or a controlled prototype. For public production use, deploy behind HTTPS, use a persistent database, add rate limiting at the reverse proxy, backups, monitoring, and robust secret management. Restrict admin access. Do not expose the HTTP local server directly to the internet.

## Android integration status
`app/.../data/MiliAdminApi.kt` contains HTTP calls for public config, activation request and license check. The current app UI/runtime does not automatically invoke those methods. Connect them to a user-entered HTTPS API base URL, activation UI, app startup config sync and premium feature gate before claiming end-to-end remote control or paid licensing.
