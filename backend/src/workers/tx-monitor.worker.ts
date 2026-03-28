import { Worker, Queue } from "bullmq";
import { createPublicClient, http, Hex } from "viem";
import { CHAINS, ChainId } from "../config/chains.js";
import { ENV } from "../config/env.js";
import { StreamService } from "../services/stream.service.js";
import prisma from "../services/prisma.service.js";

const QUEUE_NAME = "tx-monitor";

const connection = {
  host: new URL(ENV.REDIS_URL).hostname || "127.0.0.1",
  port: Number(new URL(ENV.REDIS_URL).port) || 6379,
};

/** Queue for submitting tx monitoring jobs. */
export const txMonitorQueue = new Queue(QUEUE_NAME, { connection });

export interface TxMonitorJob {
  type: "userop" | "stream-create";
  chainId: number;
  txHash: string;
  /** For stream-create: the stream record ID to update once confirmed. */
  streamId?: string;
  /** Number of confirmation attempts so far. */
  attempt?: number;
}

/**
 * Enqueue a transaction for monitoring.
 * The worker will poll for the receipt and take action when confirmed.
 */
export async function enqueueTxMonitor(data: TxMonitorJob) {
  await txMonitorQueue.add("monitor", data, {
    delay: 5_000, // Wait 5s before first check
    attempts: 10,
    backoff: { type: "exponential", delay: 10_000 },
  });
}

/**
 * Start the tx-monitor worker.
 * Call this from index.ts (or a separate worker process).
 */
export function startTxMonitorWorker() {
  const worker = new Worker<TxMonitorJob>(
    QUEUE_NAME,
    async (job) => {
      const { type, chainId, txHash, streamId } = job.data;
      const id = chainId as ChainId;
      const chain = CHAINS[id];
      if (!chain?.rpcUrl) {
        throw new Error(`No RPC for chain ${chainId}`);
      }

      const client = createPublicClient({ transport: http(chain.rpcUrl) });

      // Wait for receipt
      const receipt = await client.getTransactionReceipt({ hash: txHash as Hex });

      if (receipt.status === "reverted") {
        console.error(`[tx-monitor] Tx ${txHash} reverted on chain ${chainId}`);
        // For stream creation, mark as cancelled
        if (type === "stream-create" && streamId) {
          await prisma.stream.update({
            where: { id: streamId },
            data: { status: "CANCELLED" },
          });
        }
        return { status: "reverted", txHash };
      }

      // Tx succeeded — handle by type
      if (type === "stream-create" && streamId) {
        // Update stream with confirmed txHash
        await StreamService.linkOnChain(streamId, 0, txHash);
        console.log(`[tx-monitor] Stream ${streamId} confirmed: ${txHash}`);
      }

      console.log(`[tx-monitor] Tx ${txHash} confirmed (block ${receipt.blockNumber})`);
      return { status: "confirmed", txHash, blockNumber: receipt.blockNumber.toString() };
    },
    {
      connection,
      concurrency: 5,
    }
  );

  worker.on("failed", (job, err) => {
    console.error(`[tx-monitor] Job ${job?.id} failed:`, err.message);
  });

  worker.on("completed", (job, result) => {
    console.log(`[tx-monitor] Job ${job.id} completed:`, result);
  });

  console.log("[tx-monitor] Worker started");
  return worker;
}
