# Lumia Backend (test server)

A small standalone chat server. No Firebase, no third-party auth, no VPS required —
runs locally with plain username/password auth (bcrypt + JWT) and SQLite (via `sql.js`,
pure JS/WASM — no native compiler needed, so this also runs fine in Termux).

## Run it

```bash
cd backend
npm install
npm start
```

You'll see:
```
Lumia backend running on http://0.0.0.0:4000
```

## Connect the app to it

The app needs your computer's (or phone's, if running in Termux) **LAN IP**, not `localhost`,
since the app runs on a separate device/emulator.

- Find your LAN IP:
  - Windows: `ipconfig` → IPv4 Address
  - Mac/Linux: `ifconfig` or `ip a`
  - Termux (if running the server on the same phone as your dev work): `ip a`
- Put that IP into `BASE_URL` in `app/src/main/java/com/lumora/app/repo/ApiClient.kt`, e.g.:
  ```kotlin
  const val BASE_URL = "http://192.168.1.42:4000/"
  ```
- Your phone (running the Lumia app) and the machine running the server must be on the
  **same Wi-Fi network**.

## API quick reference

| Method | Route | Body | Notes |
|---|---|---|---|
| POST | `/auth/register` | `{ username, password, displayName }` | Returns `{ token, userId }` |
| POST | `/auth/login` | `{ username, password }` | Returns `{ token, userId }` |
| GET | `/users/search?q=` | — | Bearer token required |
| GET | `/chats` | — | List your chat threads |
| POST | `/chats/start` | `{ username }` | Start/find a chat with a user |
| GET | `/chats/:chatId/messages` | — | Message history |

Real-time messages go over Socket.io (`send_message` / `new_message` events), connecting
with `auth: { token }` in the handshake.

## Testing without the app

You can sanity-check the server itself with curl before touching the app:

```bash
curl -X POST http://localhost:4000/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"kingsley","password":"test1234"}'
```

That should return a token. Register a second user, then use `/chats/start` with that
second user's `token` to open a chat, and you're ready to test in the app.

## Data

Everything lives in `backend/lumia.sqlite` — delete that file to reset all users/chats/messages.
