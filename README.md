# Tranzo — Own Your Money. Spend It Anywhere.

Tranzo is a unified crypto finance platform combining a self-custody wallet, crypto-to-fiat spending card, and Dripper hardware wallet integration into one seamless Android app.

## Features

- **Self-Custody Wallet** — Non-custodial, multi-chain (Ethereum, Bitcoin, Solana, Polygon, Arbitrum, Base)
- **Crypto Card** — Virtual + physical VISA debit card with auto crypto-to-fiat conversion
- **Dripper Integration** — Bluetooth LE / USB-C hardware wallet for offline key storage
- **Send / Receive / Swap / Buy** — Full DeFi and fiat on-ramp support
- **Activity Tracking** — Unified transaction history with search and filters
- **Security First** — Android Keystore encryption, biometric auth, BIP-39 seed phrases

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Navigation | Compose Navigation |
| Network | Retrofit + OkHttp |
| Storage | Room + DataStore |
| Security | Android Keystore, BiometricPrompt |
| Hardware | Bluetooth LE (Dripper) |

## Project Structure

```
app/src/main/java/com/tranzo/custody/
├── data/           # Repository implementations, Room DB, Retrofit API
├── di/             # Hilt dependency injection modules
├── domain/         # Models and repository interfaces
├── navigation/     # Compose Navigation graph
├── security/       # KeyStore, biometric, seed phrase management
└── ui/
    ├── theme/      # Colors, typography, Material 3 theme
    ├── components/ # Shared composables
    ├── onboarding/ # Welcome, create/import wallet, PIN setup
    ├── home/       # Wallet, send, receive, swap, buy
    ├── card/       # Card visual, spending tracker, settings
    ├── activity/   # Transaction history, detail view
    └── settings/   # Preferences, security, Dripper pairing
```

## Build

```bash
./gradlew assembleDebug
```

## Contact

- Website: [tranzo.money](https://tranzo.money)
- Email: connect@tranzo.money
