import { Router } from "express";
import { WalletService } from "../services/wallet.service.js";
import { BundlerService } from "../services/bundler.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";
import prisma from "../services/prisma.service.js";
import { Hex } from "viem";

const router = Router();

/**
 * POST /wallet/create — Register or update a smart wallet.
 * Protected: requires authenticated user.
 * The owner address is taken from the JWT (not request body) to prevent spoofing.
 */
router.post("/create", requireAuth, async (req, res) => {
  const { salt, chainId } = req.body;
  const owner = req.user!.owner as Hex;

  try {
    const smartWalletAddr = await WalletService.computeCounterfactualAddress(
      owner,
      Number(salt ?? 1),
      Number(chainId)
    );

    const user = await prisma.user.upsert({
      where: { ownerAddr: owner },
      create: {
        ownerAddr: owner,
        smartWalletAddr,
        chainId,
      },
      update: {
        smartWalletAddr,
        chainId,
      },
    });

    res.json(user);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * POST /wallet/send-userop — Submit a UserOperation to the bundler.
 * Protected: requires authenticated user.
 */
router.post("/send-userop", requireAuth, async (req, res) => {
  const { chainId, userOp } = req.body;
  try {
    const hash = await BundlerService.sendUserOperation(chainId, userOp);
    res.json({ hash });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * GET /wallet/receipt/:chainId/:hash — Fetch a UserOp receipt.
 * Protected: requires authenticated user.
 */
router.get("/receipt/:chainId/:hash", requireAuth, async (req, res) => {
  const { chainId, hash } = req.params;
  try {
    const receipt = await BundlerService.getUserOperationReceipt(
      Number(chainId),
      hash as Hex
    );
    res.json(receipt);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
