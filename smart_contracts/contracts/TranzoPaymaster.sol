// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@account-abstraction/contracts/core/BasePaymaster.sol";
import "@openzeppelin/contracts/utils/cryptography/ECDSA.sol";

/**
 * @title TranzoPaymaster
 * @dev A verifying paymaster that sponsors gas fees for Tranzo users' UserOperations.
 * It requires a valid signature from the Tranzo backend API to authorize gas sponsorship,
 * essentially allowing gasless transactions for card settlements or user swaps.
 */
contract TranzoPaymaster is BasePaymaster {
    using ECDSA for bytes32;

    address public verifyingSigner; // The Tranzo backend signer address

    // Mapping to prevent replay attacks (if our backend doesn't handle nonces strictly enough)
    mapping(address => uint256) public sponsoredTxs;

    constructor(
        IEntryPoint _entryPoint,
        address _verifyingSigner
    ) BasePaymaster(_entryPoint) {
        verifyingSigner = _verifyingSigner;
    }

    /**
     * @dev Set a new verifying signer for gas sponsorship
     */
    function setVerifyingSigner(address newSigner) external onlyOwner {
        require(newSigner != address(0), "TranzoPaymaster: null signer");
        verifyingSigner = newSigner;
    }

    /**
     * @dev Valides whether this Paymaster should sponsor gas.
     * The EntryPoint calls this function before executing the UserOp.
     * The payload is expected to be 64 bytes: `(uint48 validUntil, uint48 validAfter, bytes signature)`
     * and we use `paymasterAndData` from UserOp to extract it.
     */
    function _validatePaymasterUserOp(
        UserOperation calldata userOp,
        bytes32 /* userOpHash */,
        uint256 requiredPreFund
    ) internal virtual override returns (bytes memory context, uint256 validationData) {
        require(
            userOp.paymasterAndData.length >= 20 + 84, // address + (validUntil + validAfter) + 65bytes sig
            "TranzoPaymaster: invalid paymasterAndData length"
        );

        // Extracting data from paymasterAndData
        // address paymaster = address(bytes20(userOp.paymasterAndData[:20]));
        uint48 validUntil = uint48(bytes6(userOp.paymasterAndData[20:26]));
        uint48 validAfter = uint48(bytes6(userOp.paymasterAndData[26:32]));
        bytes calldata signature = userOp.paymasterAndData[32:];

        // Hash data to verify backend signature
        bytes32 hash = keccak256(
            abi.encode(
                userOp.sender,
                userOp.nonce,
                keccak256(userOp.initCode),
                keccak256(userOp.callData),
                userOp.callGasLimit,
                userOp.verificationGasLimit,
                userOp.preVerificationGas,
                userOp.maxFeePerGas,
                userOp.maxPriorityFeePerGas,
                block.chainid,
                address(this),
                validUntil,
                validAfter
            )
        );

        bytes32 ethHash = hash.toEthSignedMessageHash();
        address recovered = ethHash.recover(signature);

        if (recovered != verifyingSigner) {
            return ("", SIG_VALIDATION_FAILED); // Return 1 in the validationData
        }

        // Sponsor gas success. Context is empty because we don't need postOp logic for now.
        // Returning standard 0 to signify success
        return ("", 0);
    }
}
