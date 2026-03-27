// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

/**
 * @title TranzoCardSession
 * @author Tranzo Team
 * @notice Module enabling card-processor session keys with daily and per-transaction
 *         spending limits. Intended to be inherited by TranzoAccount so that
 *         `validateUserOp` can delegate authority to pre-approved session keys
 *         without requiring the owner's signature for every card purchase.
 *
 * @dev Session keys are keyed by address. Each session tracks:
 *      - Daily and per-transaction USDC limits
 *      - Cumulative spend with automatic UTC-day reset
 *      - Validity window (validAfter, validUntil)
 *      - Allowed ERC-20 token address
 */
abstract contract TranzoCardSession {
    // ─── Data Structures ─────────────────────────────────────────────────────

    /**
     * @notice Represents an active card-payment session with spending constraints.
     * @param sessionKey    The card processor's signing key.
     * @param dailyLimit    Maximum USDC (6 decimals) allowed per UTC day.
     * @param perTxLimit    Maximum USDC per individual transaction.
     * @param spentToday    Cumulative amount spent in the current UTC day.
     * @param lastResetTimestamp Timestamp of the last daily-spend reset.
     * @param token         The ERC-20 token that may be spent (e.g., USDC).
     * @param validAfter    Session start time (unix timestamp).
     * @param validUntil    Session expiry time (unix timestamp).
     * @param active        Whether the session is currently active.
     */
    struct CardSession {
        address sessionKey;
        uint256 dailyLimit;
        uint256 perTxLimit;
        uint256 spentToday;
        uint256 lastResetTimestamp;
        address token;
        uint48 validAfter;
        uint48 validUntil;
        bool active;
    }

    // ─── Storage ─────────────────────────────────────────────────────────────

    /// @notice Maps a session key address to its CardSession configuration.
    mapping(address => CardSession) public sessions;

    // ─── Events ──────────────────────────────────────────────────────────────

    /// @notice Emitted when a new card session is registered.
    event SessionAdded(
        address indexed sessionKey,
        uint256 dailyLimit,
        uint256 perTxLimit,
        address token,
        uint48 validAfter,
        uint48 validUntil
    );

    /// @notice Emitted when a card session is revoked.
    event SessionRemoved(address indexed sessionKey);

    /// @notice Emitted when a session key spends tokens.
    event SessionSpend(address indexed sessionKey, uint256 amount);

    // ─── Errors ──────────────────────────────────────────────────────────────

    /// @notice The session key is not active or does not exist.
    error SessionNotActive();

    /// @notice The current timestamp is outside the session's validity window.
    error SessionExpiredOrNotStarted();

    /// @notice The spend amount exceeds the per-transaction limit.
    error ExceedsPerTxLimit();

    /// @notice The spend amount would exceed the daily limit.
    error ExceedsDailyLimit();

    /// @notice A session for this key already exists.
    error SessionAlreadyExists();

    // ─── Internal Auth Hook ──────────────────────────────────────────────────

    /**
     * @dev Must be overridden by the inheriting account to enforce
     *      that only the owner (or EntryPoint) can add/remove sessions.
     *      TranzoAccount enforces this via its own `onlyEntryPointOrOwner` modifier.
     */
    function _requireOwnerOrEntryPoint() internal view virtual;

    // ─── Session Management ──────────────────────────────────────────────────

    /**
     * @notice Register a new card-payment session key with spending limits.
     * @param sessionKey  The card processor's public key address.
     * @param dailyLimit  Maximum USDC per UTC day (6-decimal format, e.g., 500e6 = $500).
     * @param perTxLimit  Maximum USDC per transaction.
     * @param token       The ERC-20 token allowed for spending.
     * @param validAfter  Earliest timestamp the session becomes active.
     * @param validUntil  Timestamp when the session expires.
     */
    function addSession(
        address sessionKey,
        uint256 dailyLimit,
        uint256 perTxLimit,
        address token,
        uint48 validAfter,
        uint48 validUntil
    ) external {
        _requireOwnerOrEntryPoint();

        if (sessions[sessionKey].active) revert SessionAlreadyExists();

        sessions[sessionKey] = CardSession({
            sessionKey: sessionKey,
            dailyLimit: dailyLimit,
            perTxLimit: perTxLimit,
            spentToday: 0,
            lastResetTimestamp: block.timestamp,
            token: token,
            validAfter: validAfter,
            validUntil: validUntil,
            active: true
        });

        emit SessionAdded(sessionKey, dailyLimit, perTxLimit, token, validAfter, validUntil);
    }

    /**
     * @notice Revoke and remove a card-payment session key.
     * @param sessionKey The session key address to deactivate.
     */
    function removeSession(address sessionKey) external {
        _requireOwnerOrEntryPoint();

        if (!sessions[sessionKey].active) revert SessionNotActive();

        delete sessions[sessionKey];

        emit SessionRemoved(sessionKey);
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    /**
     * @notice Check whether a signer is an active, unexpired session key.
     * @param signer The address recovered from the UserOp signature.
     * @return True if the signer is a valid session key within its validity window.
     */
    function _isActiveSessionKey(address signer) internal view returns (bool) {
        CardSession storage session = sessions[signer];

        if (!session.active) return false;
        if (block.timestamp < session.validAfter || block.timestamp > session.validUntil) {
            return false;
        }
        return true;
    }

    /**
     * @notice Validate that a session key is authorized for a specific spend amount
     *         and record the spend.
     * @dev Called internally during validateUserOp when the signer is a session key.
     *      Automatically resets `spentToday` at the start of each new UTC day.
     * @param signer The session key address.
     * @param amount The USDC amount (6 decimals) being spent.
     */
    function validateSessionKeySpend(address signer, uint256 amount) external {
        _requireOwnerOrEntryPoint();

        CardSession storage session = sessions[signer];

        if (!session.active) revert SessionNotActive();
        if (block.timestamp < session.validAfter || block.timestamp > session.validUntil) {
            revert SessionExpiredOrNotStarted();
        }
        if (amount > session.perTxLimit) revert ExceedsPerTxLimit();

        // Auto-reset daily spend at UTC day boundary.
        uint256 currentDay = block.timestamp / 86400;
        uint256 lastResetDay = session.lastResetTimestamp / 86400;
        if (currentDay > lastResetDay) {
            session.spentToday = 0;
            session.lastResetTimestamp = block.timestamp;
        }

        if (session.spentToday + amount > session.dailyLimit) {
            revert ExceedsDailyLimit();
        }

        session.spentToday += amount;

        emit SessionSpend(signer, amount);
    }
}
