import { Router } from "express";
import { z } from "zod";
import { GasService } from "../services/gas.service.js";
import { optionalAuth } from "../middleware/auth.middleware.js";

const router = Router();

const chainQuery = z.object({
  chain: z.coerce.number().int().positive(),
});

/**
 * GET /v1/gas?chain=8453
 * Returns current gas price + base fee for a chain.
 */
router.get("/gas", optionalAuth, async (req, res) => {
  const parsed = chainQuery.safeParse(req.query);
  if (!parsed.success) {
    res.status(400).json({ error: "Missing or invalid 'chain' query parameter" });
    return;
  }

  try {
    const gas = await GasService.getGasPrice(parsed.data.chain);
    res.json(gas);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * GET /v1/gas/estimate?chain=8453
 * Returns estimated UserOp gas costs.
 */
router.get("/gas/estimate", optionalAuth, async (req, res) => {
  const parsed = chainQuery.safeParse(req.query);
  if (!parsed.success) {
    res.status(400).json({ error: "Missing or invalid 'chain' query parameter" });
    return;
  }

  try {
    const estimate = await GasService.estimateUserOpGas(parsed.data.chain);
    res.json(estimate);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
