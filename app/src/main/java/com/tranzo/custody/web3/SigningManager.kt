package com.tranzo.custody.web3

import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SigningManager @Inject constructor() {

    fun createNewAccount(): Credentials {
        val ecKeyPair = Keys.createEcKeyPair()
        return Credentials.create(ecKeyPair)
    }

    fun fromPrivateKey(privateKeyHex: String): Credentials {
        return Credentials.create(privateKeyHex)
    }

    /**
     * In a production-grade wallet, we should not store the private key in plaintext.
     * We'll use the KeyStoreManager to encrypt it before saving it in SharedPreferences or a DB.
     * For now, this is a skeleton for that integration.
     */
}
