const express = require("express");
const http = require("http");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const { Server } = require("socket.io");
const crypto = require("crypto");
const { initDb, run, queryAll, queryOne } = require("./db");

// Change this in a real deployment — for local testing it's fine as-is.
const JWT_SECRET = "lumia-dev-secret-change-me";
const PORT = process.env.PORT || 4000;

const app = express();
app.use(cors());
app.use(express.json());

const server = http.createServer(app);
const io = new Server(server, { cors: { origin: "*" } });

function newId() {
  return crypto.randomBytes(12).toString("hex");
}

function authMiddleware(req, res, next) {
  const header = req.headers.authorization || "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : null;
  if (!token) return res.status(401).json({ error: "Missing token" });
  try {
    const payload = jwt.verify(token, JWT_SECRET);
    req.userId = payload.userId;
    next();
  } catch {
    return res.status(401).json({ error: "Invalid or expired token" });
  }
}

// ---------- AUTH ----------

app.post("/auth/register", (req, res) => {
  const { username, password, displayName } = req.body || {};
  if (!username || !password) {
    return res.status(400).json({ error: "username and password are required" });
  }
  const existing = queryOne("SELECT id FROM users WHERE username = ?", [username]);
  if (existing) return res.status(409).json({ error: "Username already taken" });

  const id = newId();
  const passwordHash = bcrypt.hashSync(password, 10);
  run(
    "INSERT INTO users (id, username, password_hash, display_name, created_at) VALUES (?, ?, ?, ?, ?)",
    [id, username, passwordHash, displayName || username, Date.now()]
  );

  const token = jwt.sign({ userId: id }, JWT_SECRET, { expiresIn: "30d" });
  res.json({ token, userId: id, username, displayName: displayName || username });
});

app.post("/auth/login", (req, res) => {
  const { username, password } = req.body || {};
  const user = queryOne("SELECT * FROM users WHERE username = ?", [username]);
  if (!user || !bcrypt.compareSync(password, user.password_hash)) {
    return res.status(401).json({ error: "Invalid username or password" });
  }
  const token = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: "30d" });
  res.json({ token, userId: user.id, username: user.username, displayName: user.display_name });
});

app.get("/users/search", authMiddleware, (req, res) => {
  const q = (req.query.q || "").toString();
  const rows = queryAll(
    "SELECT id, username, display_name FROM users WHERE username LIKE ? LIMIT 20",
    [`%${q}%`]
  );
  res.json(rows);
});

// ---------- CHATS ----------

// List chat threads for the logged-in user, with the other participant + last message.
app.get("/chats", authMiddleware, (req, res) => {
  const uid = req.userId;
  const chats = queryAll(
    "SELECT * FROM chats WHERE user_a = ? OR user_b = ?",
    [uid, uid]
  );

  const result = chats.map((chat) => {
    const otherId = chat.user_a === uid ? chat.user_b : chat.user_a;
    const other = queryOne("SELECT id, username, display_name FROM users WHERE id = ?", [otherId]);
    const lastMsg = queryOne(
      "SELECT * FROM messages WHERE chat_id = ? ORDER BY created_at DESC LIMIT 1",
      [chat.id]
    );
    return {
      chatId: chat.id,
      participant: other,
      lastMessage: lastMsg ? lastMsg.text : "",
      lastMessageTime: lastMsg ? lastMsg.created_at : chat.created_at
    };
  });

  res.json(result);
});

// Start (or fetch existing) chat with another user by username.
app.post("/chats/start", authMiddleware, (req, res) => {
  const uid = req.userId;
  const { username } = req.body || {};
  const other = queryOne("SELECT * FROM users WHERE username = ?", [username]);
  if (!other) return res.status(404).json({ error: "User not found" });
  if (other.id === uid) return res.status(400).json({ error: "Can't chat with yourself" });

  let chat = queryOne(
    "SELECT * FROM chats WHERE (user_a = ? AND user_b = ?) OR (user_a = ? AND user_b = ?)",
    [uid, other.id, other.id, uid]
  );

  if (!chat) {
    const id = newId();
    run("INSERT INTO chats (id, user_a, user_b, created_at) VALUES (?, ?, ?, ?)", [
      id, uid, other.id, Date.now()
    ]);
    chat = { id, user_a: uid, user_b: other.id };
  }

  res.json({ chatId: chat.id, participant: { id: other.id, username: other.username, display_name: other.display_name } });
});

// Message history for a chat.
app.get("/chats/:chatId/messages", authMiddleware, (req, res) => {
  const rows = queryAll(
    "SELECT * FROM messages WHERE chat_id = ? ORDER BY created_at ASC",
    [req.params.chatId]
  );
  res.json(rows);
});

// ---------- REALTIME (Socket.io) ----------
// Client connects with `auth: { token }`, joins one room per chatId it's viewing.

io.use((socket, next) => {
  const token = socket.handshake.auth?.token;
  if (!token) return next(new Error("Missing token"));
  try {
    const payload = jwt.verify(token, JWT_SECRET);
    socket.userId = payload.userId;
    next();
  } catch {
    next(new Error("Invalid token"));
  }
});

io.on("connection", (socket) => {
  console.log(`[socket] user ${socket.userId} connected`);

  socket.on("join_chat", (chatId) => {
    socket.join(chatId);
  });

  socket.on("send_message", ({ chatId, text }) => {
    if (!chatId || !text) return;
    const message = {
      id: newId(),
      chat_id: chatId,
      sender_id: socket.userId,
      text,
      created_at: Date.now()
    };
    run(
      "INSERT INTO messages (id, chat_id, sender_id, text, created_at) VALUES (?, ?, ?, ?, ?)",
      [message.id, message.chat_id, message.sender_id, message.text, message.created_at]
    );
    io.to(chatId).emit("new_message", message);
  });

  socket.on("disconnect", () => {
    console.log(`[socket] user ${socket.userId} disconnected`);
  });
});

// ---------- START ----------

initDb().then(() => {
  server.listen(PORT, "0.0.0.0", () => {
    console.log(`Lumia backend running on http://0.0.0.0:${PORT}`);
    console.log(`On your phone/other devices, use your computer's LAN IP, e.g. http://192.168.1.X:${PORT}`);
  });
});
