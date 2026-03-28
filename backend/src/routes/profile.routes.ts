import { Router } from "express";
import { z } from "zod";
import { requireAuth } from "../middleware/auth.middleware.js";
import prisma from "../services/prisma.service.js";

const router = Router();

const updateProfileSchema = z.object({
  email: z.string().email().optional(),
  notificationsEnabled: z.boolean().optional(),
});

/**
 * GET /auth/me
 * Returns the authenticated user's profile.
 */
router.get("/me", requireAuth, async (req, res) => {
  try {
    const user = await prisma.user.findUnique({
      where: { id: req.user!.sub },
      select: {
        id: true,
        email: true,
        emailVerified: true,
        ownerAddr: true,
        smartWalletAddr: true,
        chainId: true,
        createdAt: true,
      },
    });

    if (!user) {
      res.status(404).json({ error: "User not found" });
      return;
    }

    res.json({ user });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * PATCH /auth/me
 * Update the authenticated user's profile fields.
 */
router.patch("/me", requireAuth, async (req, res) => {
  const parsed = updateProfileSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request body", details: parsed.error.flatten() });
    return;
  }

  const updates = parsed.data;
  if (Object.keys(updates).length === 0) {
    res.status(400).json({ error: "No fields to update" });
    return;
  }

  try {
    const user = await prisma.user.update({
      where: { id: req.user!.sub },
      data: updates,
      select: {
        id: true,
        email: true,
        emailVerified: true,
        ownerAddr: true,
        smartWalletAddr: true,
        chainId: true,
        createdAt: true,
      },
    });

    res.json({ user });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
