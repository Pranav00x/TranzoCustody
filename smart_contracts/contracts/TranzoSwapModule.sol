// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "./TranzoAccount.sol";

// Interface for DEX aggregators like 1inch or Uniswap Universal Router
interface ISwapAggregator {
    function swap(
        address caller,
        address inToken,
        address outToken,
        uint256 inAmount,
        uint256 minOutAmount,
        bytes calldata data
    ) external returns (uint256 outAmount);
}

/**
 * @title TranzoSwapModule
 * @dev An auto-conversion module for spending generic ERC20s like ETH/MATIC 
 * as USDC at merchant terminals, relying on entryPoint multicalls for atomic swaps.
 * 
 * Flow for a user holding ETH buying a coffee for 5 USDC:
 * [executeBatch in TranzoAccount]:
 * 1. TranzoSwapModule.swapForCard(WETH, USDC, 5.05 USDC, 5 USDC) (swap max cost)
 * 2. TranzoCardSession.spendWithCard(...) (pays the merchant 5 USDC)
 */
contract TranzoSwapModule {

    address public swapAggregator;

    event AggregatorUpdated(address oldAggregator, address newAggregator);
    event SwappedForCard(address indexed user, address tokenIn, address tokenOut, uint256 amountIn, uint256 amountOut);

    constructor(address _swapAggregator) {
        swapAggregator = _swapAggregator;
    }

    /**
     * @dev Swaps user tokens via a trusted aggregator (1inch/Uniswap) inside the `TranzoAccount`.
     * This module MUST be executed from inside `TranzoAccount` via `delegatecall` or direct approval,
     * but standard Account Abstraction generally natively supports `executeBatch` where the first 
     * target is this module or the dex itself. To enforce logic and keep the user safe:
     */
    function swapForCardLimit(
        address tokenIn,
        address tokenOut,
        uint256 maxAmountIn,
        uint256 requiredAmountOut,
        bytes calldata swapData // Payload from backend/1inch API
    ) external returns (uint256 actualAmountOut) {
        
        // Ensure this contract is approved to pull funds from wallet (msg.sender = TranzoAccount)
        IERC20(tokenIn).transferFrom(msg.sender, address(this), maxAmountIn);
        
        // Approve aggregator
        IERC20(tokenIn).approve(swapAggregator, maxAmountIn);

        // Execute Swap natively.
        // Assuming swapAggregator transfers the funds back to this module or msg.sender.
        actualAmountOut = ISwapAggregator(swapAggregator).swap(
            msg.sender,
            tokenIn,
            tokenOut,
            maxAmountIn,
            requiredAmountOut,
            swapData
        );
        
        require(actualAmountOut >= requiredAmountOut, "TranzoSwapModule: Slippage too high");

        // Forward swapped tokens to user wallet
        IERC20(tokenOut).transfer(msg.sender, actualAmountOut);

        // Refund any unused unswapped tokenIn
        uint256 remainingIn = IERC20(tokenIn).balanceOf(address(this));
        if (remainingIn > 0) {
            IERC20(tokenIn).transfer(msg.sender, remainingIn);
        }

        emit SwappedForCard(msg.sender, tokenIn, tokenOut, maxAmountIn - remainingIn, actualAmountOut);
    }

    /**
     * @dev Admin function to update the DEX router address
     */
    function updateAggregator(address newAggregator) external {
        // Add owner checks here depending on architecture
        address old = swapAggregator;
        swapAggregator = newAggregator;
        emit AggregatorUpdated(old, newAggregator);
    }
}
