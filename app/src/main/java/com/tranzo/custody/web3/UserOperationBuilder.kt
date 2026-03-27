package com.tranzo.custody.web3

import org.web3j.crypto.Credentials
import org.web3j.crypto.Hash
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

data class PackedUserOperation(
    val sender: String,
    val nonce: BigInteger,
    val initCode: ByteArray,
    val callData: ByteArray,
    val accountGasLimits: ByteArray, // Packed: verificationGasLimit (16) + callGasLimit (16)
    val preVerificationGas: BigInteger,
    val gasFees: ByteArray, // Packed: maxPriorityFeePerGas (16) + maxFeePerGas (16)
    val paymasterAndData: ByteArray,
    var signature: ByteArray = byteArrayOf()
)

@Singleton
class UserOperationBuilder @Inject constructor() {

    /**
     * Hashes the UserOperation for signing.
     * Note: This must match the EntryPoint's getUserOpHash logic.
     */
    fun getUserOpHash(op: PackedUserOperation, entryPoint: String, chainId: Long): ByteArray {
        // Simplified packing for demo, but includes nonce and chainId for uniqueness
        val packed = op.sender.toByteArray() + op.nonce.toByteArray() + BigInteger.valueOf(chainId).toByteArray()
        return Hash.sha3(packed)
    }

    fun signUserOp(op: PackedUserOperation, credentials: Credentials, entryPoint: String, chainId: Long): ByteArray {
        val hash = getUserOpHash(op, entryPoint, chainId)
        val sigData = Sign.signPrefixedMessage(hash, credentials.ecKeyPair)
        
        val retval = ByteArray(65)
        System.arraycopy(sigData.r, 0, retval, 0, 32)
        System.arraycopy(sigData.s, 0, retval, 32, 32)
        retval[64] = sigData.v[0]
        
        return retval
    }
}
