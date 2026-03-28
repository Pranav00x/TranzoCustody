import { Router } from "express";
import { z } from "zod";
import { Hex } from "viem";
import { PortfolioService } from "../services/portfolio.service.js";
import { BalanceService } from "../services/balance.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";

const router = Router();

const portfolioQuery = z.object({
  chain: z.coerce.number().int().positive(),
});

/**
 * GET /v1/portfolio/:address?chain=8453
 * Returns portfolio summary with token values + 24h change.
 * Requires auth — the address must match the caller's wallet.
 */
router.get("/portfolio/:address", requireAuth, async (req, res) => {
  const parsed = portfolioQuery.safeParse(req.query);
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
    const rawBalances = await BalanceService.getBalances(address, parsed.data.chain);
    const balances = rawBalances.map((b) => ({
      symbol: b.symbol,
      name: b.symbol, // Use symbol as name fallback
      contractAddress: b.contractAddress,
      balance: b.rawBalance,
      decimals: b.decimals,
    }));
    const portfolio = await PortfolioService.getPortfolio(balances);
    res.json(portfolio);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * GET /v1/prices/tokens?ids=ethereum,usd-coin
 * Returns token prices from CoinGecko (cached).
 */
router.get("/prices/tokens", async (req, res) => {
  const ids = req.query.ids as string | undefined;
  const coinIds = ids ? ids.split(",").map((s) => s.trim()) : undefined;

  try {
    const prices = await PortfolioService.getTokenPrices(coinIds);
    res.json({ prices });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
