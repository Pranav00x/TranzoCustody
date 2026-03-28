import { createPublicClient, http, formatGwei, parseEther } from "viem";
import { base } from "viem/chains";
import { CHAINS } from "../config/chains.js";

export class GasService {
  /**
   * Get current gas prices for a chain.
   */
  static async getGasPrice(chainId: number) {
    const chain = CHAINS[chainId as keyof typeof CHAINS];
    if (!chain?.rpcUrl) {
      throw new Error(`No RPC URL configured for chain ${chainId}`);
    }

    const client = createPublicClient({
      transport: http(chain.rpcUrl),
    });

    const gasPrice = await client.getGasPrice();
    const block = await client.getBlock({ blockTag: "latest" });

    return {
      chainId,
      gasPrice: gasPrice.toString(),
      gasPriceGwei: formatGwei(gasPrice),
      baseFee: block.baseFeePerGas?.toString() ?? null,
      baseFeeGwei: block.baseFeePerGas
        ? formatGwei(block.baseFeePerGas)
        : null,
      blockNumber: block.number.toString(),
    };
  }

  /**
   * Estimate gas cost for a UserOperation (simplified).
   * In production, this should call the bundler's eth_estimateUserOperationGas.
   */
  static async estimateUserOpGas(chainId: number) {
    const gas = await this.getGasPrice(chainId);
    const gasPrice = BigInt(gas.gasPrice);

    // Typical ERC-4337 UserOp gas estimates
    const estimates = {
      verification: 150_000n,
      execution: 100_000n,
      preVerification: 50_000n,
    };

    const totalGas =
      estimates.verification + estimates.execution + estimates.preVerification;
    const estimatedCost = totalGas * gasPrice;

    return {
      chainId,
      gasPrice: gas.gasPriceGwei,
      estimates: {
        verificationGasLimit: estimates.verification.toString(),
        callGasLimit: estimates.execution.toString(),
        preVerificationGas: estimates.preVerification.toString(),
      },
      totalGas: totalGas.toString(),
      estimatedCostWei: estimatedCost.toString(),
      estimatedCostEth: (Number(estimatedCost) / 1e18).toFixed(8),
    };
  }
}
