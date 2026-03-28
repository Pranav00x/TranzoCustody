import { CardProvider, CardType, CardStatus } from "@prisma/client";
import prisma from "./prisma.service.js";

export class CardService {
  /**
   * Create a virtual card for a user via the configured provider.
   * In production this calls Immersve/Reap APIs — for now we create the DB record
   * and return a placeholder externalId that the provider integration will replace.
   */
  static async createCard(
    userId: string,
    provider: CardProvider,
    type: CardType = "VIRTUAL",
    dailyLimit: bigint = BigInt(50_000_000) // $50 USDC (6 decimals)
  ) {
    // TODO: Call Immersve/Reap API to provision the card and get externalId + last4
    const externalId = `pending_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;

    return prisma.card.create({
      data: {
        userId,
        provider,
        externalId,
        type,
        dailyLimit,
        status: "ACTIVE",
      },
    });
  }

  /** List all cards for a user. */
  static async listCards(userId: string) {
    return prisma.card.findMany({
      where: { userId },
      orderBy: { createdAt: "desc" },
    });
  }

  /** Get a single card — verifies it belongs to the user. */
  static async getCard(cardId: string, userId: string) {
    const card = await prisma.card.findUnique({ where: { id: cardId } });
    if (!card || card.userId !== userId) return null;
    return card;
  }

  /** Freeze a card. */
  static async freezeCard(cardId: string, userId: string) {
    const card = await this.getCard(cardId, userId);
    if (!card) throw new Error("Card not found");
    if (card.status !== "ACTIVE") throw new Error(`Cannot freeze a ${card.status} card`);

    return prisma.card.update({
      where: { id: cardId },
      data: { status: "FROZEN" },
    });
  }

  /** Unfreeze a card. */
  static async unfreezeCard(cardId: string, userId: string) {
    const card = await this.getCard(cardId, userId);
    if (!card) throw new Error("Card not found");
    if (card.status !== "FROZEN") throw new Error(`Card is ${card.status}, not frozen`);

    return prisma.card.update({
      where: { id: cardId },
      data: { status: "ACTIVE" },
    });
  }

  /** Cancel a card permanently. */
  static async cancelCard(cardId: string, userId: string) {
    const card = await this.getCard(cardId, userId);
    if (!card) throw new Error("Card not found");
    if (card.status === "CANCELLED") throw new Error("Card already cancelled");

    return prisma.card.update({
      where: { id: cardId },
      data: { status: "CANCELLED" },
    });
  }

  /** Update daily spending limit. */
  static async updateDailyLimit(cardId: string, userId: string, dailyLimit: bigint) {
    const card = await this.getCard(cardId, userId);
    if (!card) throw new Error("Card not found");

    return prisma.card.update({
      where: { id: cardId },
      data: { dailyLimit },
    });
  }

  // ──────────────── Card Sessions (Session Keys) ────────────────

  /** Create a card spending session with on-chain session key. */
  static async createSession(
    userId: string,
    provider: CardProvider,
    sessionKeyAddr: string,
    dailyLimit: bigint,
    perTxLimit: bigint,
    validUntil: Date,
    txHash?: string
  ) {
    return prisma.cardSession.create({
      data: {
        userId,
        provider,
        sessionKeyAddr,
        dailyLimit,
        perTxLimit,
        validUntil,
        txHash,
      },
    });
  }

  /** List active sessions for a user. */
  static async listActiveSessions(userId: string) {
    return prisma.cardSession.findMany({
      where: {
        userId,
        active: true,
        validUntil: { gt: new Date() },
      },
      orderBy: { createdAt: "desc" },
    });
  }

  /** Revoke a session. */
  static async revokeSession(sessionId: string, userId: string) {
    const session = await prisma.cardSession.findUnique({ where: { id: sessionId } });
    if (!session || session.userId !== userId) throw new Error("Session not found");

    return prisma.cardSession.update({
      where: { id: sessionId },
      data: { active: false },
    });
  }
}
