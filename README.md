# Eta (η) — Voice-Powered Offline Business Assistant for Retailers

> **Built for Shipathon-KUG** | Native Android (Kotlin + Jetpack Compose + Room SQLite) + Static Showcase Web Landing Page.

Eta is an offline-first, voice-enabled point-of-sale (POS) and inventory ledger designed specifically for Indian micro-retailers (Kirana stores). It replaces pen-and-paper registers and sluggish cloud POS apps with an on-device business engine that records voice sales in under 3 seconds in **Hindi, Tamil, Bengali, and English** with **zero internet dependency**.

---

## 🚀 Key Highlights & Architecture

- **100% Offline-First Architecture**: Built on Room SQLite (`EtaDatabase.kt`). Never stalls during network blackouts or power drops. Safe non-destructive schema migrations and atomic transactions.
- **Natural Colloquial Voice Billing & Indian SOV Grammar**: Instant on-device parsing of Indian Subject-Object-Verb billing phrases (e.g. *"500 rupay ki chini Ramesh ke khate mein likh do"* or *"Ramesh 2 packet doodh 60 rupay"*), credit intent classification (`isCredit = true`), and number parsing including colloquial fractions (*aadha*, *dedh*, *dhai*).
- **Multilingual UI & Speech**: Dynamic live switching between English, Hindi (हिन्दी), Tamil (தமிழ்), and Bengali (বাংলা) across all screens with BCP-47 locale mapping and offline speech recognition fallback.
- **Automated Customer Khata & Split Checkout**: Real dues balance calculation distinguishing checkout payments from credit ledger repayments. Supports partial payments at checkout (`paidAmount < totalAmount`) with automatic remaining Khata due balance calculation. 1-tap **WhatsApp Payment Reminder** sharing with embedded NPCI UPI deep links (`wa.me/<phone>?text=...`).
- **Offline NPCI UPI QR Generator**: Real-time canvas-rendered dynamic UPI QR card based on merchant UPI ID (customizable in Settings) supporting 1-tap intent launch (`upi://pay?...`) without third-party APIs.
- **Real Dynamic Financial Analytics & COGS**: SQLite-driven Cost of Goods Sold (`SUM(quantity * costPrice)`), Gross Profit, Net Profit, Profit Margin %, dynamic 7-bucket sales trend bars, and period-over-period percentage comparisons.
- **Inventory & Quick Restock**: Real-time stock counts with low-stock warnings and quick-restock (+5, +10, +25, +50) dialogs.
- **Two-Way Full JSON Database Backup & Audit**: Full 7-table atomic JSON export and import via Android `FileProvider`, plus database stock reconciliation and audit to detect and flag anomalies.
- **Zero-Dependency Showcase Landing Page**: Embedded standalone SVG QR generator allowing instant camera scan & APK sideloading.

---

## 🛠️ Tech Stack & Libraries

| Layer | Technology |
|---|---|
| **Platform** | Android 8.0+ (API Level 26–35) |
| **Language** | Kotlin 1.9.22 |
| **UI Framework** | Jetpack Compose + Material3 |
| **Database** | Room SQLite 2.6.1 (7 Entities: `ProductEntity`, `CustomerEntity`, `SaleEntity`, `SaleItemEntity`, `ExpenseEntity`, `PaymentEntity`, `StockMovementEntity`) |
| **Asynchronous** | Kotlin Coroutines & Reactive StateFlow |
| **Speech Engine** | Android `SpeechRecognizer` + Regex / Levenshtein Colloquial Intent Parser |
| **Payments** | NPCI UPI Standard Deep Links (`upi://pay`) + Offline Canvas-Rendered QR Code |
| **Preferences** | Jetpack DataStore Preferences |
| **Web Landing Page** | HTML5, CSS3, Vanilla JS, Zero-CDN Inline SVG QR Code Generator |

---

## 📂 Project Directory Structure

```text
Shipathon-KUG/
├── app/
│   ├── build.gradle.kts                        # Android build script & dependencies (Room, Compose, DataStore)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml             # Permissions: RECORD_AUDIO, FileProvider setup
│       │   ├── res/xml/file_paths.xml          # FileProvider paths for JSON DB backups
│       │   └── java/com/eta/assistant/
│       │       ├── MainActivity.kt             # Single Activity hosting EtaNavHost
│       │       ├── core/
│       │       │   ├── theme/                  # Color, Typography, Material3 Theme
│       │       │   └── util/
│       │       │       ├── LocaleStrings.kt    # 4-language localized string maps (EN, HI, TA, BN)
│       │       │       ├── CurrencyFormatter.kt# Currency & 2-decimal rounding precision helpers
│       │       │       ├── DateUtils.kt        # Timestamp and date formatting utilities
│       │       │       └── UpiHelper.kt        # NPCI UPI deep links & Canvas-rendered offline QR cards
│       │       ├── data/
│       │       │   ├── local/
│       │       │   │   ├── EtaDatabase.kt      # Room DB definition with migration & fallback seeding
│       │       │   │   ├── dao/                # ProductDao, CustomerDao, SaleDao, PaymentDao, ExpenseDao, StockMovementDao
│       │       │   │   └── entity/             # Product, Customer, Sale, SaleItem, Payment, Expense, StockMovement entities
│       │       │   ├── preferences/            # UserPreferencesRepository (DataStore: Language, Store Name, Merchant UPI ID)
│       │       │   └── repository/             # EtaRepository & EtaRepositoryImpl (atomic DB operations, JSON backup/restore)
│       │       └── ui/
│       │           ├── navigation/             # EtaNavHost & BottomNavItem
│       │           └── screens/
│       │               ├── dashboard/          # DashboardScreen & DashboardViewModel
│       │               ├── voice/              # VoiceSaleScreen, VoiceViewModel & VoiceCommandParser (Indian SOV grammar)
│       │               ├── inventory/          # InventoryScreen & InventoryViewModel
│       │               ├── customers/          # CustomerListScreen, CustomerDetailScreen & CustomersViewModel
│       │               ├── sales/              # RecordSaleScreen, SaleConfirmationScreen & SalesViewModel (Split checkout)
│       │               ├── reports/            # ReportsScreen & ReportsViewModel (COGS, Gross/Net profit, 7-bucket trend)
│       │               ├── settings/           # SettingsScreen & SettingsViewModel (Merchant UPI ID, Language, Store Info)
│       │               ├── sync/               # SyncScreen (Database Audit Reconciliation & 7-Table JSON Backup/Restore)
│       │               └── pricing/            # PricingScreen
│       └── test/                               # Comprehensive JUnit test suite
│           └── java/com/eta/assistant/
│               ├── BusinessLogicUnitTest.kt    # Tests for currency rounding, UPI URI, ledger dues, and JSON backup integrity
│               └── VoiceCommandParserTest.kt   # Tests for Hindi, Tamil, Bengali SOV commands, Indic numerals, customer credit & colloquial numbers
├── landing-page/
│   └── public/
│       ├── index.html                          # High-conversion interactive landing page + offline QR code generator
│       └── eta-business-assistant.apk          # Pre-compiled standalone installable APK (~18.2 MB)
├── eta_remediation_plan.md                     # Completed 6-phase engineering remediation specification
├── PENDING_TASKS.md                            # Comprehensive roadmap & pitch status
└── README.md                                   # Project documentation
```

---

## 🧪 Testing & Verification

All core flows are covered by an automated test suite:
```bash
# Run unit tests across business logic, SQLite DAOs, and Voice Parsers
./gradlew test

# Compile standalone debug APK
./gradlew assembleDebug
```
*Current Test Status:* **100% tests passing** (`BUILD SUCCESSFUL`).

---

## 📱 Sideload Installation & Deployment

1. **Direct Android Installation**:
   - The compiled APK is available at `landing-page/public/eta-business-assistant.apk`.
   - Users can open `landing-page/public/index.html` on any laptop or phone, scan the QR code with their mobile camera, and tap **Install** (zero Google account required).
2. **Landing Page Hosting**:
   - Easily deployable to Firebase Hosting (`firebase deploy --only hosting`) or Vercel (`vercel deploy`).
