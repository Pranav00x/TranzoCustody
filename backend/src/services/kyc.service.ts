import { KycStatus } from "@prisma/client";
import prisma from "./prisma.service.js";

/**
 * KYC Provider Service — placeholder for real provider integration.
 *
 * In production, replace the placeholder methods with actual API calls to:
 * - Sumsub (https://sumsub.com)
 * - Immersve KYC
 * - Onfido
 * - Jumio
 *
 * Flow:
 * 1. User initiates KYC → createVerificationSession()
 * 2. Returns a URL/SDK token the mobile app uses to open the verification UI
 * 3. Provider sends webhook on completion → processWebhookResult()
 * 4. Backend updates user KYC status
 */
export class KycService {
  /**
   * Create a verification session with the KYC provider.
   * Returns a session URL or SDK access token.
   *
   * TODO: Replace with real provider API call
   */
  static async createVerificationSession(
    userId: string,
    provider: string = "sumsub"
  ): Promise<{ sessionUrl: string; sessionId: string }> {
    // Update status to PENDING
    await prisma.user.update({
      where: { id: userId },
      data: { kycStatus: KycStatus.PENDING, kycProvider: provider },
    });

    // Placeholder — in production, call the provider API:
    //
    // Sumsub example:
    //   const response = await axios.post('https://api.sumsub.com/resources/accessTokens', {
    //     userId: smartWalletAddr,
    //     levelName: 'basic-kyc',
    //   }, { headers: { 'X-App-Token': SUMSUB_API_KEY } });
    //   return { sessionUrl: response.data.url, sessionId: response.data.id };
    //
    // Immersve example:
    //   const response = await immersveApi.post('/kyc/sessions', { walletAddress });
    //   return { sessionUrl: response.data.verificationUrl, sessionId: response.data.sessionId };

    const sessionId = `kyc_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
    return {
      sessionUrl: `https://verify.placeholder.com/session/${sessionId}`,
      sessionId,
    };
  }

  /**
   * Process a KYC webhook result from the provider.
   * Called by webhook handlers when the provider notifies us of a result.
   */
  static async processWebhookResult(
    walletAddress: string,
    approved: boolean,
    provider: string
  ): Promise<void> {
    await prisma.user.updateMany({
      where: { smartWalletAddr: walletAddress.toLowerCase() },
      data: {
        kycStatus: approved ? KycStatus.APPROVED : KycStatus.REJECTED,
        kycProvider: provider,
      },
    });
  }

  /** Get KYC status for a user. */
  static async getStatus(userId: string) {
    const user = await prisma.user.findUnique({
      where: { id: userId },
      select: { kycStatus: true, kycProvider: true },
    });
    return user;
  }
}
