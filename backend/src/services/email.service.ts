import { ENV } from "../config/env.js";

/**
 * Email service placeholder.
 * Replace with nodemailer / SendGrid / Resend in production.
 */
export class EmailService {
  static async sendPasswordResetEmail(
    to: string,
    token: string
  ): Promise<void> {
    const resetLink = `tranzo://reset-password?token=${token}`;
    // TODO: Replace with real email sending
    console.log(`[EMAIL] Password reset for ${to}: ${resetLink}`);
  }

  static async sendVerificationEmail(
    to: string,
    token: string
  ): Promise<void> {
    const verifyLink = `tranzo://verify-email?token=${token}`;
    console.log(`[EMAIL] Verification for ${to}: ${verifyLink}`);
  }

  static async sendLoginAlertEmail(
    to: string,
    ip: string,
    userAgent: string
  ): Promise<void> {
    console.log(`[EMAIL] Login alert for ${to} from ${ip} (${userAgent})`);
  }
}
