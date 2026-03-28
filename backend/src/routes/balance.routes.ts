import { Router } from "express";
import { z } from "zod";
import { Hex } from "viem";
import { BalanceService } from "../services/balance.service.js";
import { optionalAuth } from "../middleware/auth.middleware.js";

const router = Router();

// ────────────────────────── Validation ──────────────────────────

const balanceQuery = z.object({
  chain: z.coerce.number().int(),
});

const priceQuery = z.object({
  symbols: z.string().min(1), // comma-separated: "ETH,USDC,MATIC"
});

// ──────────────────────── Routes ────────────────────────────────

/**
 * GET /v1/balances/:address?chain=80002
 * Public endpoint (optionalAuth) — the Android app calls this without auth initially.
 * Returns native + ERC-20 balances for a wallet.
 */
router.get("/balances/:address", optionalAuth, async (req, res) => {
  const parsed = balanceQuery.safeParse(req.query);
  if (!parsed.success) {
    res.status(400).json({ error: "Missing or invalid 'chain' query parameter" });
    return;
  }

  const address = req.params.address as Hex;
  if (!/^0x[a-fA-F0-9]{40}$/.test(address)) {
    res.status(400).json({ error: "Invalid address format" });
    return;
  }

  try {
    const balances = await BalanceService.getBalances(address, parsed.data.chain);
    res.json({ balances });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * GET /v1/prices?symbols=ETH,USDC,MATIC
 * Public endpoint — returns current USD prices + 24h change.
 */
router.get("/prices", async (req, res) => {
  const parsed = priceQuery.safeParse(req.query);
  if (!parsed.success) {
    res.status(400).json({ error: "Missing 'symbols' query parameter" });
    return;
  }

  try {
    const symbols = parsed.data.symbols.split(",").map((s) => s.trim());
    const prices = await BalanceService.getPrices(symbols);
    res.json({ prices });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
