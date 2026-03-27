// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import {PackedUserOperation} from "account-abstraction/interfaces/PackedUserOperation.sol";

/**
 * @title ITranzoAccount
 * @notice Interface for the Tranzo ERC-4337 smart account.
 * @dev Extends IAccount behavior with card session support, batch execution,
 *      EIP-1271 signature validation, and ownership management.
 */
interface ITranzoAccount {
    // ─── Events ───────────────────────────────────────────────────────────────
    event Initialized(address indexed owner);
    event OwnershipTransferStarted(address indexed currentOwner, address indexed pendingOwner, uint256 effectiveAt);
    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);
    event OwnershipTransferCancelled(address indexed currentOwner, address indexed cancelledPendingOwner);
    event Executed(address indexed dest, uint256 value, bytes data);
    event BatchExecuted(uint256 count);

    // ─── Errors ───────────────────────────────────────────────────────────────
    error NotOwner();
    error NotEntryPoint();
    error NotEntryPointOrOwner();
    error NotEntryPointOrOwnerOrSelf();
    error InvalidSignatureLength();
    error ZeroAddress();
    error TransferTooSoon();
    error NoPendingTransfer();
    error CallFailed(uint256 index, bytes returnData);
    error ArrayLengthMismatch();

    // ─── Core Functions ───────────────────────────────────────────────────────

    /**
     * @notice Initialize the account with an owner address.
     * @param owner The EOA that controls this smart account.
     */
    function initialize(address owner) external;

    /**
     * @notice Execute a single call from this account.
     * @param dest Target contract address.
     * @param value ETH value to send.
     * @param data Calldata to forward.
     */
    function execute(address dest, uint256 value, bytes calldata data) external;

    /**
     * @notice Execute a batch of calls atomically.
     * @param dest Array of target addresses.
     * @param values Array of ETH values.
     * @param data Array of calldata.
     */
    function executeBatch(address[] calldata dest, uint256[] calldata values, bytes[] calldata data) external;

    /**
     * @notice Start a 48-hour timelock to transfer ownership.
     * @param newOwner The proposed new owner address.
     */
    function transferOwnership(address newOwner) external;

    /**
     * @notice Complete the ownership transfer after timelock expires.
     */
    function completeOwnershipTransfer() external;

    /**
     * @notice Cancel a pending ownership transfer.
     */
    function cancelOwnershipTransfer() external;

    // ─── View Functions ───────────────────────────────────────────────────────

    /// @notice Returns the current owner of this account.
    function owner() external view returns (address);

    /// @notice Returns the pending owner (if a transfer is in progress).
    function pendingOwner() external view returns (address);

    /// @notice Returns the timestamp when the pending transfer becomes effective.
    function transferEffectiveAt() external view returns (uint256);

    /// @notice Returns the EntryPoint contract this account is tied to.
    function entryPoint() external view returns (address);

    /// @notice Returns the nonce from the EntryPoint for this account.
    function getNonce() external view returns (uint256);
}
