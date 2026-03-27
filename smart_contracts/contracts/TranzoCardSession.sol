// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@account-abstraction/contracts/interfaces/IEntryPoint.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "./TranzoAccount.sol";

/**
 * @title TranzoCardSession
 * @dev A smart contract module that handles temporary session keys for Tranzo Cards.
 * It manages card limits directly on-chain and validates signatures.
 */
contract TranzoCardSession {
    struct CardSession {
        address sessionKey;       // Card processor's key
        uint256 dailyLimit;       // Max spend per day in smallest unit
        uint256 perTxLimit;       // Max per transaction
        uint256 spentToday;       // Tracking amount spent today
        uint256 lastResetDay;     // Timestamp of last reset day
        address[] allowedTokens;  // USDC, USDT etc.
        bool active;              // Is this card active?
    }

    // Mapping from (User Wallet Address => (Session Key => CardSession))
    mapping(address => mapping(address => CardSession)) public sessions;

    event SessionCreated(address indexed wallet, address indexed sessionKey, uint256 dailyLimit);
    event SessionRevoked(address indexed wallet, address indexed sessionKey);
    event SpentFromSession(address indexed wallet, address indexed sessionKey, address token, uint256 amount);

    /**
     * @dev Called by the wallet owner (TranzoAccount) to allow a session key (card processor)
     */
    function enableSession(
        address sessionKey,
        uint256 dailyLimit,
        uint256 perTxLimit,
        address[] calldata allowedTokens
    ) external {
        // msg.sender MUST be a valid smart wallet that owns its funds.
        sessions[msg.sender][sessionKey] = CardSession({
            sessionKey: sessionKey,
            dailyLimit: dailyLimit,
            perTxLimit: perTxLimit,
            spentToday: 0,
            lastResetDay: block.timestamp / 1 days,
            allowedTokens: allowedTokens,
            active: true
        });

        emit SessionCreated(msg.sender, sessionKey, dailyLimit);
    }

    /**
     * @dev Block a physical/virtual card instantly on-chain
     */
    function revokeSession(address sessionKey) external {
        // Can be revoked by the wallet owner
        sessions[msg.sender][sessionKey].active = false;
        emit SessionRevoked(msg.sender, sessionKey);
    }

    /**
     * @dev Validates the signature of a session key.
     * Checks if the userOp was signed by the sessionKey and it hasn't exceeded limits.
     * Note: A production implementation requires unpacking the calldata to analyze exactly who is being paid.
     */
    function validateSessionSignature(
        address wallet,
        UserOperation calldata userOp,
        bytes32 /* userOpHash */, // Normally we'd use this but since we passed recoveredSigner...
        address recoveredSigner
    ) external view returns (bool) {
        // The sender checking validateSessionSignature must equal the userOp.sender
        if (msg.sender != userOp.sender || wallet != userOp.sender) {
            return false;
        }

        // Check if the signature was actually made by a valid session key tracked by this wallet
        CardSession storage session = sessions[wallet][recoveredSigner];
        
        if (!session.active) return false;
        if (session.sessionKey != recoveredSigner) return false;

        // In a true production environment, we MUST decode userOp.callData here 
        // to check token addresses, limits, and amounts.
        // E.g. (address target, uint256 value, bytes data) = abi.decode(userOp.callData[4:], (...))
        // And update `spentToday` which means this function should probably be called pre/post flight,
        // or the actual spend mechanism routes through a spend() function on this module, instead of passing completely via fallback.
        // For architectural setup, return true.

        return true;
    }

    /**
     * @dev Executed by the card processor (using the session key) to directly transfer tokens
     * and update internal limits without doing a full UserOp. Better for fast real-world transactions.
     */
    function spendWithCard(
        address wallet,
        address tokenOut,
        address merchant,
        uint256 amount
    ) external {
        // msg.sender is the sessionKey sending gas (or via paymaster).
        CardSession storage session = sessions[wallet][msg.sender];
        require(session.active, "Session not active or invalid");
        require(session.sessionKey == msg.sender, "Caller is not session key");
        
        // Reset limits if a new day has started
        uint256 currentDay = block.timestamp / 1 days;
        if (currentDay > session.lastResetDay) {
            session.spentToday = 0;
            session.lastResetDay = currentDay;
        }

        require(amount <= session.perTxLimit, "Exceeds per-tx limit");
        require(session.spentToday + amount <= session.dailyLimit, "Exceeds daily limit");

        // Validate token allowance
        bool isAllowedToken = false;
        for (uint i = 0; i < session.allowedTokens.length; i++) {
            if (session.allowedTokens[i] == tokenOut) {
                isAllowedToken = true;
                break;
            }
        }
        require(isAllowedToken, "Token not allowed for this card");

        // Update limits
        session.spentToday += amount;

        // Execute via TranzoAccount execution block
        // Assuming this contract was whitelisted inside `TranzoAccount`
        // We call the generic token transfer:
        bytes memory data = abi.encodeCall(IERC20.transfer, (merchant, amount));
        TranzoAccount(payable(wallet)).execute(tokenOut, 0, data);

        emit SpentFromSession(wallet, msg.sender, tokenOut, amount);
    }
}
