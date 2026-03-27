import express from "express";
import cors from "cors";
import helmet from "helmet";
import { ENV } from "./config/env.js";
import walletRoutes from "./routes/wallet.routes.js";
import paymasterRoutes from "./routes/paymaster.routes.js";

const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json());

app.use("/wallet", walletRoutes);
app.use("/paymaster", paymasterRoutes);

app.get("/health", (req, res) => {
  res.json({ status: "ok" });
});

app.listen(ENV.PORT, () => {
  console.log(`🚀 Tranzo Backend running on port ${ENV.PORT}`);
});
