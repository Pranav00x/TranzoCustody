import express from "express";
import cors from "cors";
import helmet from "helmet";
import { ENV } from "./config/env.js";
import { errorHandler } from "./middleware/error.middleware.js";
import { generalLimiter, authLimiter } from "./middleware/ratelimit.middleware.js";
import authRoutes from "./routes/auth.routes.js";
import walletRoutes from "./routes/wallet.routes.js";
import paymasterRoutes from "./routes/paymaster.routes.js";
import cardRoutes from "./routes/card.routes.js";
import streamRoutes from "./routes/stream.routes.js";
import balanceRoutes from "./routes/balance.routes.js";
import { startTxMonitorWorker } from "./workers/tx-monitor.worker.js";

const app = express();

// ──────────────────── Global Middleware ──────────────────────────
app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(generalLimiter);

// ──────────────────── Public Routes ─────────────────────────────
app.use("/auth", authLimiter, authRoutes);
app.use("/v1", balanceRoutes);

app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

// ──────────────────── Protected Routes ──────────────────────────
app.use("/wallet", walletRoutes);
app.use("/paymaster", paymasterRoutes);
app.use("/cards", cardRoutes);
app.use("/streams", streamRoutes);

// ──────────────────── Error Handler ─────────────────────────────
app.use(errorHandler);

// ──────────────────── Start Server ──────────────────────────────
app.listen(ENV.PORT, () => {
  console.log(`Tranzo Backend running on port ${ENV.PORT}`);

  // Start background workers (non-blocking — fails gracefully if Redis is down)
  try {
    startTxMonitorWorker();
  } catch (err) {
    console.warn("tx-monitor worker failed to start (Redis may not be available):", err);
  }
});
