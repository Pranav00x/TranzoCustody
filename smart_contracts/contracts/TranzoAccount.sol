// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@account-abstraction/contracts/core/BaseAccount.sol";
import "@openzeppelin/contracts/utils/cryptography/ECDSA.sol";
import "@openzeppelin/contracts-upgradeable/proxy/utils/Initializable.sol";
import "@openzeppelin/contracts-upgradeable/proxy/utils/UUPSUpgradeable.sol";
import "./TranzoCardSession.sol";

/**
 * @title TranzoAccount
 * @dev ERC-4337 smart contract wallet designed for the Tranzo Custody App.
 * Supports EntryPoint v0.6.x, Upgradeability, and Card Session Modules.
 */
contract TranzoAccount is BaseAccount, Initializable, UUPSUpgradeable {
    using ECDSA for bytes32;

    IEntryPoint private _entryPoint;
    address public owner;
    
    // Address of the CardSession module
    TranzoCardSession public cardSessionModule;

    event TranzoAccountInitialized(IEntryPoint indexed entryPoint, address indexed owner);
    event OwnerRotated(address indexed oldOwner, address indexed newOwner);

    modifier onlyOwner() {
        require(msg.sender == owner, "TranzoAccount: not owner");
        _;
    }

    /// @custom:oz-upgrades-unsafe-allow constructor
    constructor(IEntryPoint anEntryPoint) {
        _entryPoint = anEntryPoint;
        _disableInitializers();
    }

    /**
     * @dev Initialize the wallet with an owner
     */
    function initialize(address anOwner) public initializer {
        owner = anOwner;
        emit TranzoAccountInitialized(_entryPoint, anOwner);
    }

    /**
     * @dev Setup / Register the card session module.
     * Only callable by the owner of the wallet.
     */
    function setCardSessionModule(TranzoCardSession _module) external onlyOwner {
        cardSessionModule = _module;
    }

    /**
     * @inheritdoc BaseAccount
     */
    function entryPoint() public view override returns (IEntryPoint) {
        return _entryPoint;
    }

    /**
     * @dev Validate the signature of a UserOperation.
     * Overrides BaseAccount's _validateSignature.
     * Supports both Owner signing OR Card Processor signing if Session matches
     */
    function _validateSignature(
        UserOperation calldata userOp,
        bytes32 userOpHash
    ) internal virtual override returns (uint256 validationData) {
        bytes32 hash = userOpHash.toEthSignedMessageHash();
        
        // 1. Check if the signature matches the owner
        address recoveredSigner = hash.recover(userOp.signature);
        if (owner == recoveredSigner) {
            return 0; // Success, signed by owner
        }

        // 2. Fallback to Card Session Module if configured
        if (address(cardSessionModule) != address(0)) {
            bool isValidSession = cardSessionModule.validateSessionSignature(
                address(this),
                userOp,
                userOpHash,
                recoveredSigner
            );
            if (isValidSession) {
                return 0; // Success, signed by valid card session
            }
        }
        
        return SIG_VALIDATION_FAILED;
    }

    /**
     * @dev Execute a transaction. EntryPoint calls this after validation.
     * Can also be accessed by the owner directly (or CardSession module).
     */
    function execute(address dest, uint256 value, bytes calldata func) external {
        _requireFromEntryPointOrOwnerOrModule();
        _call(dest, value, func);
    }

    /**
     * @dev Execute batch transaction to save gas fees.
     */
    function executeBatch(address[] calldata dest, uint256[] calldata value, bytes[] calldata func) external {
        _requireFromEntryPointOrOwnerOrModule();
        require(dest.length == func.length && dest.length == value.length, "TranzoAccount: wrong array lengths");
        for (uint256 i = 0; i < dest.length; i++) {
            _call(dest[i], value[i], func[i]);
        }
    }

    function _call(address target, uint256 value, bytes memory data) internal {
        (bool success, bytes memory result) = target.call{value: value}(data);
        if (!success) {
            assembly {
                revert(add(result, 32), mload(result))
            }
        }
    }

    function _requireFromEntryPointOrOwnerOrModule() internal view {
        require(
            msg.sender == address(entryPoint()) || 
            msg.sender == owner || 
            msg.sender == address(cardSessionModule),
            "TranzoAccount: not EntryPoint, Owner, or CardModule"
        );
    }

    /**
     * @dev Allows users to rotate keys, required for social recovery or compromised keys.
     */
    function rotateOwner(address newOwner) external {
        _requireFromEntryPointOrOwnerOrModule();
        require(newOwner != address(0), "TranzoAccount: cannot be zero address");
        address oldOwner = owner;
        owner = newOwner;
        emit OwnerRotated(oldOwner, newOwner);
    }

    /**
     * @dev Required for UUPS upgrades. Only owner or entrypoint can upgrade perfectly self-custodied.
     */
    function _authorizeUpgrade(address newImplementation) internal view override {
        (newImplementation);
        _requireFromEntryPointOrOwnerOrModule();
    }
    
    // Receive fallback
    receive() external payable {}
}
