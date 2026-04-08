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
        <div style="font-family: sans-serif; padding: 40px; color: #333; background-color: #f9f9f9;">
          <div style="max-width: 500px; margin: 0 auto; background: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
            <h2 style="color: #000; margin-bottom: 24px; font-size: 24px;">Verify your identity</h2>
            <p style="font-size: 16px; line-height: 1.5; color: #666;">Use the following code to complete your sign-in or verification. This code is valid for 10 minutes.</p>
            <div style="font-size: 38px; font-weight: bold; letter-spacing: 8px; margin: 32px 0; color: #000; text-align: center; background: #f0f0f0; padding: 20px; border-radius: 8px;">
              ${otp}
            </div>
            <p style="font-size: 14px; color: #999; margin-top: 32px;">If you didn't request this, you can safely ignore this email.</p>
          </div>
          <div style="text-align: center; margin-top: 24px; color: #999; font-size: 12px;">
            &copy; ${new Date().getFullYear()} Tranzo Custody. All rights reserved.
          </div>
        </div>
      `,
    });
  }

  static async sendPasswordReset(email: string, token: string) {
    const resetUrl = `${ENV.CLIENT_URL}/reset-password?token=${token}`;
    
    await this.sendEmail({
      to: email,
      subject: "Reset your Tranzo password",
      text: `To reset your password, please visit: ${resetUrl}. This link will expire in 15 minutes.`,
      html: `
        <div style="font-family: sans-serif; padding: 40px; color: #333; background-color: #f9f9f9;">
          <div style="max-width: 500px; margin: 0 auto; background: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
            <h2 style="color: #000; margin-bottom: 24px; font-size: 24px;">Reset Password</h2>
            <p style="font-size: 16px; line-height: 1.5; color: #666;">We received a request to reset your Tranzo account password. Click the button below to proceed.</p>
            <div style="text-align: center; margin: 32px 0;">
              <a href="${resetUrl}" style="background-color: #000; color: #fff; padding: 16px 32px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Reset Password</a>
            </div>
            <p style="font-size: 14px; color: #999; margin-top: 24px;">If the button doesn't work, copy and paste this link into your browser:</p>
            <p style="font-size: 12px; color: #007aff; word-break: break-all;">${resetUrl}</p>
            <p style="font-size: 14px; color: #999; margin-top: 32px;">This link will expire in 15 minutes. If you didn't request a password reset, please ignore this email.</p>
          </div>
        </div>
      `,
    });
  }

  static async sendWelcomeEmail(email: string, smartWalletAddr: string) {
    await this.sendEmail({
      to: email,
      subject: "Welcome to Tranzo Wallet!",
      text: `Your account is ready! Your smart wallet address is: ${smartWalletAddr}`,
      html: `
        <div style="font-family: sans-serif; padding: 40px; color: #333; background-color: #f9f9f9;">
          <div style="max-width: 500px; margin: 0 auto; background: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
            <h2 style="color: #000; margin-bottom: 24px; font-size: 26px;">Welcome to Tranzo!</h2>
            <p style="font-size: 16px; line-height: 1.5; color: #666;">Your self-custody wallet is now active. You have full control over your digital assets with institutional-grade security.</p>
            
            <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 24px 0;">
              <p style="margin: 0; font-size: 12px; color: #999; text-transform: uppercase; letter-spacing: 1px;">Your Smart Wallet Address</p>
              <p style="margin: 8px 0 0 0; font-family: monospace; font-size: 14px; color: #333; word-break: break-all;">${smartWalletAddr}</p>
            </div>

            <p style="font-size: 16px; line-height: 1.5; color: #666;">Start exploring decentralized finance, streaming payments, and more.</p>
            
            <div style="text-align: center; margin: 32px 0;">
              <a href="${ENV.CLIENT_URL}" style="background-color: #000; color: #fff; padding: 16px 32px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Go to Dashboard</a>
            </div>
          </div>
        </div>
      `,
    });
  }
}
