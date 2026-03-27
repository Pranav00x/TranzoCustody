// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import {IAccount} from "account-abstraction/interfaces/IAccount.sol";
import {PackedUserOperation} from "account-abstraction/interfaces/PackedUserOperation.sol";
import {IEntryPoint} from "account-abstraction/interfaces/IEntryPoint.sol";
import {UUPSUpgradeable} from "@openzeppelin/contracts-upgradeable/proxy/utils/UUPSUpgradeable.sol";
import {Initializable} from "@openzeppelin/contracts-upgradeable/proxy/utils/Initializable.sol";
import {ECDSA} from "@openzeppelin/contracts/utils/cryptography/ECDSA.sol";
import {MessageHashUtils} from "@openzeppelin/contracts/utils/cryptography/MessageHashUtils.sol";
import {IERC1271} from "@openzeppelin/contracts/interfaces/IERC1271.sol";
import {TranzoCardSession} from "./TranzoCardSession.sol";

/**
 * @title TranzoAccount
 * @author Tranzo Team
 * @notice ERC-4337 compliant smart account with UUPS upgradeability,
 *         ECDSA signature validation, EIP-1271 support, card session keys,
 *         and a 48-hour ownership transfer timelock.
 * @dev This contract is deployed behind an ERC1967 proxy and initialized
 *      via the TranzoAccountFactory. Only the EntryPoint (v0.7) or the
 *      current owner may call execution methods.
 */
contract TranzoAccount is
    Initializable,
    UUPSUpgradeable,
    IAccount,
    IERC1271,
    TranzoCardSession
{
    using ECDSA for bytes32;
    using MessageHashUtils for bytes32;

    // ─── Constants ───────────────────────────────────────────────────────────

    /// @notice ERC-4337 v0.7 canonical EntryPoint address.
    IEntryPoint public constant ENTRY_POINT =
        IEntryPoint(0x0000000071727De22E5E9d8BAf0edAc6f37da032);

    /// @notice EIP-1271 magic value returned on valid signatures.
    bytes4 internal constant _EIP1271_MAGIC = 0x1626ba7e;

    /// @notice Sentinel values for UserOp validation.
    uint256 internal constant SIG_VALIDATION_SUCCESS = 0;
    uint256 internal constant SIG_VALIDATION_FAILED = 1;

    /// @notice Duration of the ownership transfer timelock (48 hours).
    uint256 public constant OWNERSHIP_TRANSFER_DELAY = 48 hours;

    // ─── Storage ─────────────────────────────────────────────────────────────

    /// @notice The EOA that controls this smart account.
    address public owner;

    /// @notice Address of the proposed new owner (zero if no transfer pending).
    address public pendingOwner;

    /// @notice Timestamp after which the pending ownership transfer may complete.
    uint256 public transferEffectiveAt;

    // ─── Events ──────────────────────────────────────────────────────────────

    /// @notice Emitted when the account is initialized with an owner.
    event Initialized(address indexed owner);

    /// @notice Emitted when an ownership transfer is initiated.
    event OwnershipTransferStarted(
        address indexed currentOwner,
        address indexed pendingOwner,
        uint256 effectiveAt
    );

    /// @notice Emitted when ownership is successfully transferred.
    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);

    /// @notice Emitted when a pending ownership transfer is cancelled.
    event OwnershipTransferCancelled(address indexed currentOwner, address indexed cancelledPendingOwner);

    /// @notice Emitted on a successful single-call execution.
    event Executed(address indexed dest, uint256 value, bytes data);

    /// @notice Emitted on a successful batch execution.
    event BatchExecuted(uint256 count);

    // ─── Errors ──────────────────────────────────────────────────────────────

    /// @notice Caller is not the owner.
    error NotOwner();

    /// @notice Caller is not the EntryPoint.
    error NotEntryPoint();

    /// @notice Caller is not the EntryPoint or the owner.
    error NotEntryPointOrOwner();

    /// @notice Caller is not the EntryPoint, the owner, or this contract itself.
    error NotEntryPointOrOwnerOrSelf();

    /// @notice The provided signature has an invalid length.
    error InvalidSignatureLength();

    /// @notice A zero address was provided where it is not allowed.
    error ZeroAddress();

    /// @notice The ownership timelock has not yet expired.
    error TransferTooSoon();

    /// @notice No ownership transfer is currently pending.
    error NoPendingTransfer();

    /// @notice A call inside executeBatch reverted.
    /// @param index The index of the failing call.
    /// @param returnData The revert data from the failed call.
    error CallFailed(uint256 index, bytes returnData);

    /// @notice Array lengths do not match in executeBatch.
    error ArrayLengthMismatch();

    // ─── Modifiers ───────────────────────────────────────────────────────────

    /// @dev Restricts access to the v0.7 EntryPoint.
    modifier onlyEntryPoint() {
        if (msg.sender != address(ENTRY_POINT)) revert NotEntryPoint();
        _;
    }

    /// @dev Restricts access to the EntryPoint or the owner.
    modifier onlyEntryPointOrOwner() {
        if (msg.sender != address(ENTRY_POINT) && msg.sender != owner) {
            revert NotEntryPointOrOwner();
        }
        _;
    }

    /// @dev Restricts access to the EntryPoint, the owner, or self (for UUPS upgrades).
    modifier onlyEntryPointOrOwnerOrSelf() {
        if (
            msg.sender != address(ENTRY_POINT) &&
            msg.sender != owner &&
            msg.sender != address(this)
        ) {
            revert NotEntryPointOrOwnerOrSelf();
        }
        _;
    }

    // ─── Constructor (disables initializers on implementation) ────────────────

    /// @custom:oz-upgrades-unsafe-allow constructor
    constructor() {
        _disableInitializers();
    }

    // ─── Initializer ─────────────────────────────────────────────────────────

    /**
     * @notice Initialize the proxy instance with the owning EOA.
     * @param _owner The address that will control this smart account.
     */
    function initialize(address _owner) external initializer {
        if (_owner == address(0)) revert ZeroAddress();
        owner = _owner;
        emit Initialized(_owner);
    }

    // ─── ERC-4337: validateUserOp ────────────────────────────────────────────

    /**
     * @notice Validate a UserOperation's signature.
     * @dev Called by the EntryPoint during the validation phase.
     *      1. Computes the UserOp hash via the EntryPoint.
     *      2. Recovers the signer from the attached ECDSA signature.
     *      3. Accepts the op if the signer is the owner **or** a valid session key
     *         (as defined in TranzoCardSession).
     *      4. Forwards any required prefund to the EntryPoint.
     * @param userOp The packed user operation.
     * @param userOpHash The hash of the user operation (provided by EntryPoint).
     * @param missingAccountFunds The amount that must be sent to EntryPoint as prefund.
     * @return validationData `0` on success, `1` on failure.
     */
    function validateUserOp(
        PackedUserOperation calldata userOp,
        bytes32 userOpHash,
        uint256 missingAccountFunds
    ) external onlyEntryPoint returns (uint256 validationData) {
        // Recover the signer from the EIP-191 prefixed hash.
        bytes32 ethSignedHash = userOpHash.toEthSignedMessageHash();
        address signer = ethSignedHash.recover(userOp.signature);

        // Accept if the signer is the owner.
        if (signer == owner) {
             return SIG_VALIDATION_SUCCESS;
        }
        
        // Otherwise, check if it's an active card session key.
        // Note: For ERC-4337, we return the time window in validationData instead of checking block.timestamp.
        CardSession storage session = sessions[signer];
        if (session.active) {
            return (uint256(session.validUntil) << 160) | (uint256(session.validAfter) << (160 + 48));
        }

        return SIG_VALIDATION_FAILED;

        // Pay prefund to EntryPoint if required.
        if (missingAccountFunds > 0) {
            (bool success, ) = payable(address(ENTRY_POINT)).call{value: missingAccountFunds}("");
            // Ignore failure — EntryPoint will revert if funds are insufficient.
            (success);
        }
    }

    // ─── Execution ───────────────────────────────────────────────────────────

    /**
     * @notice Execute a single call from this account.
     * @param dest  Target contract or EOA.
     * @param value Native value (ETH/MATIC) to send.
     * @param data  Calldata to forward.
     */
    function execute(
        address dest,
        uint256 value,
        bytes calldata data
    ) external onlyEntryPointOrOwner {
        (bool success, bytes memory result) = dest.call{value: value}(data);
        if (!success) {
            // Bubble up the revert reason.
            assembly {
                revert(add(result, 32), mload(result))
            }
        }
        emit Executed(dest, value, data);
    }

    /**
     * @notice Execute a batch of calls atomically.
     * @param dest   Array of target addresses.
     * @param values Array of ETH values.
     * @param data   Array of calldata payloads.
     */
    function executeBatch(
        address[] calldata dest,
        uint256[] calldata values,
        bytes[] calldata data
    ) external onlyEntryPointOrOwner {
        if (dest.length != values.length || dest.length != data.length) {
            revert ArrayLengthMismatch();
        }

        for (uint256 i = 0; i < dest.length; i++) {
            (bool success, bytes memory result) = dest[i].call{value: values[i]}(data[i]);
            if (!success) {
                revert CallFailed(i, result);
            }
        }
        emit BatchExecuted(dest.length);
    }

    // ─── Ownership Management (48h Timelock) ─────────────────────────────────

    /**
     * @notice Initiate a 48-hour timelocked ownership transfer.
     * @param newOwner The proposed new owner address.
     */
    function transferOwnership(address newOwner) external onlyEntryPointOrOwner {
        if (newOwner == address(0)) revert ZeroAddress();

        pendingOwner = newOwner;
        transferEffectiveAt = block.timestamp + OWNERSHIP_TRANSFER_DELAY;

        emit OwnershipTransferStarted(owner, newOwner, transferEffectiveAt);
    }

    /**
     * @notice Complete the ownership transfer after the timelock has expired.
     */
    function completeOwnershipTransfer() external onlyEntryPointOrOwner {
        if (pendingOwner == address(0)) revert NoPendingTransfer();
        if (block.timestamp < transferEffectiveAt) revert TransferTooSoon();

        address previousOwner = owner;
        owner = pendingOwner;
        pendingOwner = address(0);
        transferEffectiveAt = 0;

        emit OwnershipTransferred(previousOwner, owner);
    }

    /**
     * @notice Cancel a pending ownership transfer.
     */
    function cancelOwnershipTransfer() external onlyEntryPointOrOwner {
        if (pendingOwner == address(0)) revert NoPendingTransfer();

        address cancelled = pendingOwner;
        pendingOwner = address(0);
        transferEffectiveAt = 0;

        emit OwnershipTransferCancelled(owner, cancelled);
    }

    // ─── EIP-1271: Off-chain Signature Verification ──────────────────────────

    /**
     * @notice Verify an off-chain signature on behalf of this account (EIP-1271).
     * @param hash      The hash that was signed.
     * @param signature The ECDSA signature bytes.
     * @return magicValue `0x1626ba7e` if valid, `0xffffffff` otherwise.
     */
    function isValidSignature(
        bytes32 hash,
        bytes memory signature
    ) external view override returns (bytes4 magicValue) {
        bytes32 ethSignedHash = hash.toEthSignedMessageHash();
        address signer = ethSignedHash.recover(signature);

        if (signer == owner) {
            return _EIP1271_MAGIC;
        }
        return bytes4(0xffffffff);
    }

    // ─── View Helpers ────────────────────────────────────────────────────────

    /**
     * @notice Returns the EntryPoint address this account is tied to.
     * @return The IEntryPoint contract reference.
     */
    function entryPoint() external pure returns (IEntryPoint) {
        return ENTRY_POINT;
    }

    /**
     * @notice Returns the nonce for this account from the EntryPoint.
     * @return The current nonce (key = 0).
     */
    function getNonce() external view returns (uint256) {
        return ENTRY_POINT.getNonce(address(this), 0);
    }

    // ─── UUPS Authorization ──────────────────────────────────────────────────

    /**
     * @dev Only the owner, EntryPoint, or self may authorize an upgrade.
     */
    function _authorizeUpgrade(address) internal override onlyEntryPointOrOwnerOrSelf {}

    // ─── TranzoCardSession Authorization ─────────────────────────────────────
    
    function _requireOwnerOrEntryPoint() internal view override {
        if (msg.sender != address(ENTRY_POINT) && msg.sender != owner) {
            revert NotEntryPointOrOwner();
        }
    }

    // ─── Receive Ether ───────────────────────────────────────────────────────

    /// @notice Allow the contract to receive native currency.
    receive() external payable {}
}
