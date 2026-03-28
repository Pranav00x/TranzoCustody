package com.tranzo.custody.web3

import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Constructs and signs EIP-4361 (SIWE) messages for wallet authentication.
 *
 * The SIWE message format:
 *   ${domain} wants you to sign in with your Ethereum account:
 *   ${address}
 *
 *   ${statement}
 *
 *   URI: ${uri}
 *   Version: 1
 *   Chain ID: ${chainId}
 *   Nonce: ${nonce}
 *   Issued At: ${issuedAt}
 *   Expiration Time: ${expirationTime}
 */
@Singleton
class SiweManager @Inject constructor() {

    companion object {
        private const val DOMAIN = "tranzo.money"
        private const val URI = "https://tranzo.money"
        private const val STATEMENT = "Sign in to Tranzo Custody"
        private const val VERSION = "1"
        private const val EXPIRY_MINUTES = 10L
    }

    /**
     * Build a SIWE message string from the given parameters.
     */
    fun buildMessage(
        address: String,
        chainId: Int,
        nonce: String
    ): String {
        val now = Instant.now()
        val issuedAt = DateTimeFormatter.ISO_INSTANT.format(now)
        val expirationTime = DateTimeFormatter.ISO_INSTANT.format(
            now.plusSeconds(EXPIRY_MINUTES * 60)
        )

        return buildString {
            appendLine("$DOMAIN wants you to sign in with your Ethereum account:")
            appendLine(address)
            appendLine()
            appendLine(STATEMENT)
            appendLine()
            appendLine("URI: $URI")
            appendLine("Version: $VERSION")
            appendLine("Chain ID: $chainId")
            appendLine("Nonce: $nonce")
            appendLine("Issued At: $issuedAt")
            append("Expiration Time: $expirationTime")
        }
    }

    /**
     * Sign a SIWE message with the user's credentials.
     * Returns the hex-encoded signature (0x-prefixed, 65 bytes: r + s + v).
     */
    fun signMessage(message: String, credentials: Credentials): String {
        val messageBytes = message.toByteArray(Charsets.UTF_8)
        // EIP-191 personal_sign: prefix with "\x19Ethereum Signed Message:\n" + length
        val prefix = "\u0019Ethereum Signed Message:\n${messageBytes.size}"
        val prefixedMessage = prefix.toByteArray(Charsets.UTF_8) + messageBytes

        val hash = org.web3j.crypto.Hash.sha3(prefixedMessage)
        val signatureData = Sign.signMessage(hash, credentials.ecKeyPair, false)

        // Pack as r (32) + s (32) + v (1) = 65 bytes
        val r = Numeric.toBytesPadded(java.math.BigInteger(1, signatureData.r), 32)
        val s = Numeric.toBytesPadded(java.math.BigInteger(1, signatureData.s), 32)
        val v = signatureData.v

        val sig = ByteArray(65)
        System.arraycopy(r, 0, sig, 0, 32)
        System.arraycopy(s, 0, sig, 32, 32)
        sig[64] = v[0]

        return Numeric.toHexString(sig)
    }
}
