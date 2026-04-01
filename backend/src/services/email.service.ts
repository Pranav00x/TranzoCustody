import { ENV } from "../config/env.js";
import axios from "axios";

export interface EmailOptions {
  to: string;
  subject: string;
  text: string;
  html?: string;
}

export class EmailService {
  /**
   * Sends an email using Resend (prioritized) or SMTP.
   * If neither is configured, logs to console (useful for dev).
   */
  static async sendEmail(options: EmailOptions) {
    if (ENV.RESEND_API_KEY) {
      return this.sendViaResend(options);
    }

    // Fallback: Just log in development if no provider is set
    if (ENV.NODE_ENV === "development") {
      console.log("------------------------------------------");
      console.log(`DEV EMAIL TO: ${options.to}`);
      console.log(`SUBJECT: ${options.subject}`);
      console.log(`BODY: ${options.text}`);
      console.log("------------------------------------------");
      return;
    }

    throw new Error("No email provider configured (RESEND_API_KEY missing)");
  }

  private static async sendViaResend(options: EmailOptions) {
    try {
      await axios.post(
        "https://api.resend.com/emails",
        {
          from: ENV.EMAIL_FROM,
          to: [options.to],
          subject: options.subject,
          text: options.text,
          html: options.html,
        },
        {
          headers: {
            Authorization: `Bearer ${ENV.RESEND_API_KEY}`,
            "Content-Type": "application/json",
          },
        }
      );
    } catch (err: any) {
      console.error("Resend email failed:", err.response?.data || err.message);
      throw new Error("Failed to send email via Resend");
    }
  }

  static async sendOTP(email: string, otp: string) {
    await this.sendEmail({
      to: email,
      subject: `Your Tranzo Verification Code: ${otp}`,
      text: `Your verification code is ${otp}. It will expire in 10 minutes.`,
      html: `
        <div style="font-family: sans-serif; padding: 20px; color: #333;">
          <h2 style="color: #000;">Tranzo Wallet</h2>
          <p>Your verification code is:</p>
          <div style="font-size: 32px; font-weight: bold; letter-spacing: 5px; margin: 20px 0; color: #000;">
            ${otp}
          </div>
          <p style="font-size: 14px; color: #666;">This code will expire in 10 minutes. if you didn't request this, you can safely ignore this email.</p>
        </div>
      `,
    });
  }
}
