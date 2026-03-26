# Tranzo — Own Your Money. Spend It Anywhere.

Tranzo is a unified crypto finance platform combining a deposit-to-spend wallet, crypto-to-fiat spending card, and Dripper hardware wallet integration into one seamless Android app.

## Features

- **Deposit & Spend** — Deposit crypto, convert to spendable balance, tap to pay anywhere
- **Crypto Card** — Virtual + physical VISA debit card with auto crypto-to-fiat conversion
- **Two Balance System** — Wallet Balance (deposited crypto) + Spendable Balance (card spending)
- **Dripper Integration** — Bluetooth LE / USB-C hardware wallet for secure transaction signing
- **Send / Receive / Swap / Buy** — Full DeFi and fiat on-ramp support
- **Activity Tracking** — Unified transaction history with search and filters
- **Security First** — Android Keystore encryption, biometric auth, PIN protection

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
├── security/       # KeyStore, biometric helpers
└── ui/
    ├── theme/      # Colors, typography, Material 3 theme
    ├── components/ # Shared composables
    ├── onboarding/ # Welcome, sign up, PIN setup
    ├── home/       # Wallet, send, receive, swap, buy, bridge
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
