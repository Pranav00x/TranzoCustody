import { StreamStatus } from "@prisma/client";
import prisma from "./prisma.service.js";

export class StreamService {
  /**
   * Create a new token stream record.
   * The on-chain tx is submitted separately via /wallet/send-userop.
   * This stores the stream metadata for tracking.
   */
  static async createStream(
    senderId: string,
    recipientAddr: string,
    token: string,
    totalAmount: bigint,
    startTime: Date,
    endTime: Date,
    txHash?: string,
    onChainStreamId?: number
  ) {
    if (endTime <= startTime) {
      throw new Error("endTime must be after startTime");
    }

    return prisma.stream.create({
      data: {
        senderId,
        recipientAddr: recipientAddr.toLowerCase(),
        token,
        totalAmount,
        startTime,
        endTime,
        txHash,
        onChainStreamId,
      },
    });
  }

  /** List all streams sent by a user. */
  static async listSentStreams(senderId: string) {
    return prisma.stream.findMany({
      where: { senderId },
      orderBy: { createdAt: "desc" },
    });
  }

  /** List all streams where a given address is the recipient. */
  static async listReceivedStreams(recipientAddr: string) {
    return prisma.stream.findMany({
      where: { recipientAddr: recipientAddr.toLowerCase() },
      orderBy: { createdAt: "desc" },
    });
  }

  /** Get a single stream by ID — verifies sender ownership. */
  static async getStream(streamId: string, senderId: string) {
    const stream = await prisma.stream.findUnique({ where: { id: streamId } });
    if (!stream || stream.senderId !== senderId) return null;
    return stream;
  }

  /**
   * Update the on-chain stream ID and tx hash after the UserOp is confirmed.
   * Called by the tx monitoring worker.
   */
  static async linkOnChain(
    streamId: string,
    onChainStreamId: number,
    txHash: string
  ) {
    return prisma.stream.update({
      where: { id: streamId },
      data: { onChainStreamId, txHash },
    });
  }

  /** Record a withdrawal amount against a stream. */
  static async recordWithdrawal(streamId: string, amount: bigint) {
    const stream = await prisma.stream.findUnique({ where: { id: streamId } });
    if (!stream) throw new Error("Stream not found");
    if (stream.status !== "ACTIVE") throw new Error("Stream is not active");

    const newWithdrawn = BigInt(stream.withdrawn) + amount;
    if (newWithdrawn > BigInt(stream.totalAmount)) {
      throw new Error("Withdrawal exceeds total stream amount");
    }

    const status: StreamStatus =
      newWithdrawn >= BigInt(stream.totalAmount) ? "COMPLETED" : "ACTIVE";

    return prisma.stream.update({
      where: { id: streamId },
      data: { withdrawn: newWithdrawn, status },
    });
  }

  /** Cancel a stream (only the sender can cancel). */
  static async cancelStream(streamId: string, senderId: string) {
    const stream = await this.getStream(streamId, senderId);
    if (!stream) throw new Error("Stream not found");
    if (stream.status !== "ACTIVE" && stream.status !== "PAUSED") {
      throw new Error(`Cannot cancel a ${stream.status} stream`);
    }

    return prisma.stream.update({
      where: { id: streamId },
      data: { status: "CANCELLED" },
    });
  }

  /** Pause a stream. */
  static async pauseStream(streamId: string, senderId: string) {
    const stream = await this.getStream(streamId, senderId);
    if (!stream) throw new Error("Stream not found");
    if (stream.status !== "ACTIVE") throw new Error("Stream is not active");

    return prisma.stream.update({
      where: { id: streamId },
      data: { status: "PAUSED" },
    });
  }

  /** Resume a paused stream. */
  static async resumeStream(streamId: string, senderId: string) {
    const stream = await this.getStream(streamId, senderId);
    if (!stream) throw new Error("Stream not found");
    if (stream.status !== "PAUSED") throw new Error("Stream is not paused");

    return prisma.stream.update({
      where: { id: streamId },
      data: { status: "ACTIVE" },
    });
  }
}
