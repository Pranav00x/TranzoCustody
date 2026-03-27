// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import {IPaymaster} from "account-abstraction/interfaces/IPaymaster.sol";
import {PackedUserOperation} from "account-abstraction/interfaces/PackedUserOperation.sol";
import {IEntryPoint} from "account-abstraction/interfaces/IEntryPoint.sol";
import {ECDSA} from "@openzeppelin/contracts/utils/cryptography/ECDSA.sol";
import {MessageHashUtils} from "@openzeppelin/contracts/utils/cryptography/MessageHashUtils.sol";
import {Ownable} from "@openzeppelin/contracts/access/Ownable.sol";
import {IERC20} from "@openzeppelin/contracts/interfaces/IERC20.sol";
import {SafeERC20} from "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";
import {SIG_VALIDATION_FAILED} from "account-abstraction/core/Helpers.sol";

/**
 * @title TranzoPaymaster
 * @author Tranzo Team
 * @notice Provides gas sponsorship (Mode 1) or USDC-pegged gas payments (Mode 2)
 *         for Tranzo users. Uses an off-chain verifier signer to authorize sponsorship.
 */
contract TranzoPaymaster is IPaymaster, Ownable {
    using ECDSA for bytes32;
    using MessageHashUtils for bytes32;
    using SafeERC20 for IERC20;

    IEntryPoint public immutable entryPoint;
    address public verifyingSigner;
    IERC20 public usdcToken;

    // A rough static ETH/USDC price for MVP (e.g. 1 ETH = 3000 USDC)
    uint256 public constant ETH_USDC_RATE = 3000;
    
    // Limits
    mapping(address => uint256) public userDailyGasSpent;
    mapping(address => uint256) public userLastResetDay;
    uint256 public constant MAX_DAILY_SPONSORED_GAS_USDC = 1e6; // $1 max per day

    struct PaymasterContext {
        address sender;
        uint8 mode; // 1 = Sponsored, 2 = USDC Payment
    }

    constructor(
        IEntryPoint _entryPoint,
        address _verifyingSigner,
        address initialOwner,
        address _usdcToken
    ) Ownable(initialOwner) {
        entryPoint = _entryPoint;
        verifyingSigner = _verifyingSigner;
        usdcToken = IERC20(_usdcToken);
    }

    /**
     * @notice Paymaster validation logic returning context and validationData.
     */
    function validatePaymasterUserOp(
        PackedUserOperation calldata userOp,
        bytes32 userOpHash,
        uint256 maxCost
    ) external override returns (bytes memory context, uint256 validationData) {
        if (msg.sender != address(entryPoint)) revert("Not EntryPoint");

        // paymasterAndData format:
        // paymasterAddress (20 bytes) + validUntil (6 bytes) + validAfter (6 bytes) + mode (1 byte) + signature (64-65 bytes)
        if (userOp.paymasterAndData.length < 97) {
            return ("", SIG_VALIDATION_FAILED);
        }

        uint48 validUntil = uint48(bytes6(userOp.paymasterAndData[20:26]));
        uint48 validAfter = uint48(bytes6(userOp.paymasterAndData[26:32]));
        uint8 mode = uint8(bytes1(userOp.paymasterAndData[32:33]));
        bytes calldata signature = userOp.paymasterAndData[33:];

        bytes32 hash = keccak256(abi.encode(userOpHash, validUntil, validAfter));
        bytes32 ethSignedHash = hash.toEthSignedMessageHash();
        
        address recoveredSigner = ethSignedHash.recover(signature);
        if (recoveredSigner != verifyingSigner) {
            return ("", SIG_VALIDATION_FAILED);
        }

        // Keep track of gas limits for sponsored mode
        if (mode == 1) {
            uint256 currentDay = block.timestamp / 86400;
            if (userLastResetDay[userOp.sender] < currentDay) {
                userDailyGasSpent[userOp.sender] = 0;
                userLastResetDay[userOp.sender] = currentDay;
            }

            uint256 estimatedUsdcCost = (maxCost * ETH_USDC_RATE) / 1e12; // converting from 18 to 6 decimals
            if (userDailyGasSpent[userOp.sender] + estimatedUsdcCost > MAX_DAILY_SPONSORED_GAS_USDC) {
                return ("", SIG_VALIDATION_FAILED); // Exceeded budget
            }
            userDailyGasSpent[userOp.sender] += estimatedUsdcCost;
        }

        validationData = (uint256(validUntil) << 160) | (uint256(validAfter) << (160 + 48));
        
        context = abi.encode(PaymasterContext({
            sender: userOp.sender,
            mode: mode
        }));
        
        return (context, validationData);
    }

    /**
     * @notice Deduct USDC for Mode 2 after the operation executes.
     */
    function postOp(
        PostOpMode postOpMode,
        bytes calldata context,
        uint256 actualGasCost,
        uint256 actualUserOpFeePerGas
    ) external override {
        if (msg.sender != address(entryPoint)) revert("Not EntryPoint");

        PaymasterContext memory parsedContext = abi.decode(context, (PaymasterContext));

        if (parsedContext.mode == 2) {
            uint256 usdcCost = (actualGasCost * ETH_USDC_RATE) / 1e12; 
            // Note: Users must have already approved Paymaster to spend USDC
            usdcToken.safeTransferFrom(parsedContext.sender, address(this), usdcCost);
        }
    }

    function updateVerifyingSigner(address newSigner) external onlyOwner {
        verifyingSigner = newSigner;
    }
    
    function withdrawUsdc(address to, uint256 amount) external onlyOwner {
        usdcToken.safeTransfer(to, amount);
    }

    /**
     * @dev Deposit ETH to EntryPoint for gas sponsorship.
     */
    function deposit() external payable {
        entryPoint.depositTo{value: msg.value}(address(this));
    }

    /**
     * @dev Withdraw unspent ETH from EntryPoint.
     */
    function withdrawTo(address payable withdrawAddress, uint256 amount) external onlyOwner {
        entryPoint.withdrawTo(withdrawAddress, amount);
    }

    /**
     * @dev Deposit stake to EntryPoint.
     */
    function addStake(uint32 unstakeDelaySec) external payable onlyOwner {
        entryPoint.addStake{value: msg.value}(unstakeDelaySec);
    }

    function unlockStake() external onlyOwner {
        entryPoint.unlockStake();
    }

    function withdrawStake(address payable withdrawAddress) external onlyOwner {
        entryPoint.withdrawStake(withdrawAddress);
    }
}
