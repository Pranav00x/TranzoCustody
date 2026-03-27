# Tranzo Smart Contracts (ERC-4337 Card Module)

This repository contains the production-ready smart contracts powering the **Tranzo Self-Custody Card**. It's built utilizing **ERC-4337 Account Abstraction** on **Polygon / Base**.

## Architecture & Flow

The user holds absolute custody of their smart wallet. The physical or virtual "Tranzo Card" is controlled via the `TranzoCardSession` module. 

When a user taps their card at a merchant:
1. Card processors (Mastercard/Visa) hit the Tranzo backend.
2. The Tranzo backend creates an ERC-4337 `UserOperation`.
3. The `UserOperation` is signed using the **Session Key** created exclusively for the card and within pre-approved limits.
4. If a swap is required (e.g., user holding ETH but merchant needs USDC), `TranzoSwapModule` acts iteratively in the batch call to instantly convert.
5. `TranzoPaymaster` covers the gas fees.

## Contracts 

*   `TranzoAccount.sol`: The core smart wallet deployed for every user via UUPS upgrades. Supports standard owner rotation, ERC-4337 entry point verification, and customized session checks.
*   `TranzoAccountFactory.sol`: Deploys user wallets utilizing `CREATE2` to pre-determine their wallet address cheaply before even doing an on-chain deployment.
*   `TranzoCardSession.sol`: Mints transient session keys for cards matching pre-approved limits.
*   `TranzoSwapModule.sol`: DEX aggregator wrapper allowing users to auto-swap when hitting a debit network natively.
*   `TranzoPaymaster.sol`: A verifying paymaster to process free or fiat-sponsored gas transactions.

## Setup

```bash
# Bypass PS Execution Policy if you are on Windows
npm.cmd install

# Compile contracts using Hardhat
npx hardhat compile

# Run tests
npx hardhat test
```
