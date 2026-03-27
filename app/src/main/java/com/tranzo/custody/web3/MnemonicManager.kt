package com.tranzo.custody.web3

import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MnemonicManager @Inject constructor() {

    private val ethDerivationPath: IntArray = intArrayOf(
        44 or Bip32ECKeyPair.HARDENED_BIT,
        60 or Bip32ECKeyPair.HARDENED_BIT,
        0 or Bip32ECKeyPair.HARDENED_BIT,
        0,
        0
    )

    fun generateMnemonic(): String {
        val entropy = ByteArray(16)
        SecureRandom().nextBytes(entropy)
        return MnemonicUtils.generateMnemonic(entropy)
    }

    fun normalizeMnemonic(mnemonic: String): String =
        mnemonic.trim().lowercase().replace(Regex("\\s+"), " ")

    fun validateMnemonic(mnemonic: String): Boolean {
        val m = normalizeMnemonic(mnemonic)
        if (m.isBlank()) return false
        return try {
            MnemonicUtils.generateSeed(m, "")
            true
        } catch (_: Exception) {
            false
        }
    }

    fun deriveCredentials(mnemonic: String): Credentials {
        val m = normalizeMnemonic(mnemonic)
        val seed = MnemonicUtils.generateSeed(m, "")
        val master = Bip32ECKeyPair.generateKeyPair(seed)
        val child = Bip32ECKeyPair.deriveKeyPath(master, ethDerivationPath)
        return Credentials.create(child)
    }
}
