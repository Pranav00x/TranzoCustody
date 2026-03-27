import { Router } from "express";
import { WalletService } from "../services/wallet.service.js";
import { BundlerService } from "../services/bundler.service.js";
import prisma from "../services/prisma.service.js";
import { Hex } from "viem";

const router = Router();

router.post("/create", async (req, res) => {
  const { owner, salt, chainId } = req.body;

  try {
    const smartWalletAddr = await WalletService.computeCounterfactualAddress(owner as Hex, salt);

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

router.post("/send-userop", async (req, res) => {
    const { chainId, userOp } = req.body;
    try {
        const hash = await BundlerService.sendUserOperation(chainId, userOp);
        res.json({ hash });
    } catch (err: any) {
        res.status(500).json({ error: err.message });
    }
});

router.get("/receipt/:chainId/:hash", async (req, res) => {
    const { chainId, hash } = req.params;
    try {
        const receipt = await BundlerService.getUserOperationReceipt(Number(chainId), hash as Hex);
        res.json(receipt);
    } catch (err: any) {
        res.status(500).json({ error: err.message });
    }
});

export default router;
