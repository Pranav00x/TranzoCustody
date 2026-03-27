package com.tranzo.custody.web3

import com.tranzo.custody.security.KeyStoreManager
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.utils.Numeric
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SigningManager @Inject constructor(
    private val keyStoreManager: KeyStoreManager
) {

    fun createNewAccount(): Credentials = Credentials.create(Keys.createEcKeyPair())

    fun fromPrivateKey(privateKeyHex: String): Credentials =
        Credentials.create(privateKeyHex)

    fun hasUnlockedWallet(): Boolean = keyStoreManager.hasStoredPrivateKey()

    fun loadCredentials(): Credentials? {
        val pk = keyStoreManager.loadDecryptedPrivateKey() ?: return null
        val bi = BigInteger(1, pk)
        return Credentials.create(ECKeyPair.create(bi))
    }

    fun persistCredentials(credentials: Credentials) {
        val bytes = Numeric.toBytesPadded(credentials.ecKeyPair.privateKey, 32)
        keyStoreManager.storeEncryptedPrivateKey(bytes)
    }

    fun clearWalletKeys() {
        keyStoreManager.wipeAll()
    }
}
