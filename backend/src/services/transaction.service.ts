import prisma from "./prisma.service.js";

export class TransactionService {
  /**
   * Record a new transaction.
   */
  static async create(data: {
    userId: string;
    txHash: string;
    chainId: number;
    from: string;
    to: string;
    value: string;
    token?: string;
    tokenAddress?: string;
    type: "SEND" | "RECEIVE" | "SWAP" | "BRIDGE" | "CONTRACT_CALL" | "USER_OP";
  }) {
    return prisma.transaction.create({ data });
  }

  /**
   * Get transaction history for a user with pagination.
   */
  static async getHistory(
    userId: string,
    opts: { page?: number; limit?: number; chainId?: number } = {}
  ) {
    const page = opts.page ?? 1;
    const limit = Math.min(opts.limit ?? 20, 100);
    const skip = (page - 1) * limit;

    const where: any = { userId };
    if (opts.chainId) where.chainId = opts.chainId;

    const [transactions, total] = await Promise.all([
      prisma.transaction.findMany({
        where,
        orderBy: { createdAt: "desc" },
        skip,
        take: limit,
      }),
      prisma.transaction.count({ where }),
    ]);

    return {
      transactions,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit),
      },
    };
  }

  /**
   * Get a single transaction by ID.
   */
  static async getById(id: string, userId: string) {
    return prisma.transaction.findFirst({
      where: { id, userId },
    });
  }

  /**
   * Update transaction status (e.g., after confirmation).
   */
  static async updateStatus(
    txHash: string,
    status: "PENDING" | "CONFIRMED" | "FAILED" | "DROPPED",
    extra?: { blockNumber?: bigint; gasUsed?: string; timestamp?: Date }
  ) {
    return prisma.transaction.updateMany({
      where: { txHash },
      data: { status, ...extra },
    });
  }
}
