import { Router } from "express";
import { PaymasterService } from "../services/paymaster.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";
import { Hex } from "viem";

const router = Router();

/**
 * POST /paymaster/sign — Sign a paymaster approval for a UserOp.
 * Protected: requires authenticated user.
 */
router.post("/sign", requireAuth, async (req, res) => {
  const { userOpHash, validUntil, validAfter, mode } = req.body;

  try {
    const paymasterAndData = await PaymasterService.signPaymasterApproval(
      userOpHash as Hex,
      validUntil,
      validAfter,
      mode
    );

    res.json({ paymasterAndData });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
