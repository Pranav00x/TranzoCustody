import { encodeAbiParameters, keccak256, encodePacked, Hex } from "viem";
import { privateKeyToAccount } from "viem/accounts";
import { ENV } from "../config/env.js";

export class PaymasterService {
  private static signer = privateKeyToAccount(ENV.PAYMASTER_SIGNER_PRIVATE_KEY as Hex);

  /**
   * Generates the signature and data for the paymaster.
   * Signature covers [userOpHash, validUntil, validAfter, mode]
   */
  static async signPaymasterApproval(
    userOpHash: Hex,
    validUntil: number,
    validAfter: number,
    mode: number = 1 // 1 = Sponsored, 2 = USDC Payment
  ) {
    // 1. Pack the data that needs signing (matching the smart contract)
    const hash = keccak256(
      encodeAbiParameters(
        [
          { type: 'bytes32', name: 'userOpHash' },
          { type: 'uint48', name: 'validUntil' },
          { type: 'uint48', name: 'validAfter' }
        ],
        [userOpHash, validUntil, validAfter]
      )
    );

    // 2. Sign with the private key
    const signature = await this.signer.signMessage({
      message: { raw: hash },
    });

    // 3. Return the packed data for the paymasterAndData field
    // Format: paymasterAddress (20) + validUntil (6) + validAfter (6) + mode (1) + signature (64-65)
    return encodePacked(
      ["address", "uint48", "uint48", "uint8", "bytes"],
      [ENV.PAYMASTER_ADDRESS as Hex, validUntil, validAfter, mode, signature]
    );
  }
}
