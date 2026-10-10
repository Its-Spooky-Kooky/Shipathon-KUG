# Project Eta — Offline-First Smart Business Assistant

**FIRST IMPORTSNTCE LOCAL DEVELOPMENT**

Act as a senior Android engineer, Kotlin developer, UI/UX designer, and product architect. Build a complete, functional, polished native Android mobile application named **Eta**.

Do not just create a prototype, static screens, or a design document. Implement the application, connect the screens to real application logic, run the available build and tests, and fix errors wherever possible.

## 1. Product vision

Eta is an offline-first business management assistant for small shop owners, local retailers, and independent sellers.

It helps users manage:
- Sales and transactions
- Products and inventory
- Customers and outstanding dues
- Payments and payment history
- Low-stock alerts
- Daily and weekly business summaries
- Voice-assisted business operations

The main value proposition is simplicity. A business owner should be able to operate the app without needing accounting knowledge or spending time navigating complicated software.

**Tagline:** Your Business, Simplified.

**Primary platform:** Android mobile.

**Language:** Kotlin.

**UI framework:** Jetpack Compose with Material 3.

**Priority:** A reliable, attractive, genuinely functional offline MVP.

## 2. Development principles

1. Build a native Android application, not a web app wrapped in a WebView.
2. Use Kotlin throughout the Android application.
3. Use Jetpack Compose for all new UI.
4. Follow a maintainable architecture with clear separation of UI, business logic, and data.
5. Persist application data in a local Room database.
6. Core functionality must work in airplane mode after installation.
7. Never create buttons that appear functional but do nothing.
8. Avoid hardcoded dashboard numbers when corresponding database records exist.
9. Use realistic sample data only for an explicitly identified demo or onboarding mode.
10. Do not require API keys, paid services, or cloud accounts to launch the basic MVP.
11. Keep the interface clean, modern, consistent, and easy for nontechnical users.
12. Prefer working functionality over excessive features.

First inspect the existing project and its Gradle configuration. Reuse working components where appropriate. If the project is empty, create a proper Android application structure. Do not destroy unrelated existing files.

## 3. Visual design system

Create a premium, modern, AI-inspired interface with the quality of a polished fintech or business productivity application.

The interface should feel intelligent and distinctive without being cluttered.

### Color palette

Use a consistent light theme:
- Primary: deep emerald green
- Secondary: teal
- Background: warm off-white
- Surface: white
- Primary text: dark charcoal
- Secondary text: muted gray
- Success: green
- Warning: amber
- Error: restrained red

Use dark text on light surfaces for excellent readability.

Add an optional dark theme if it can be implemented cleanly without delaying the core functionality.

### Visual characteristics

- Material 3 components with customized styling.
- Rounded cards with consistent corner radii.
- Subtle tonal gradients for selected highlights.
- Clear typographic hierarchy.
- Generous spacing and padding.
- Consistent iconography using Material Icons or suitable vector assets.
- Small, purposeful animations and transitions.
- Clearly visible touch targets.
- Accessible text sizes and sufficient contrast.
- Loading, empty, success, error, and offline states.
- Responsive layouts that adapt to different Android phone sizes.

Avoid excessive gradients, giant decorative illustrations, unnecessary glassmorphism, excessive shadows, tiny text, and too many cards competing for attention.

Do not make every component colorful. Use color to establish hierarchy.

Use a reusable design system with centralized colors, typography, spacing, and component styles.

## 4. Application navigation

Create a bottom navigation bar with four main destinations:

1. Home
2. Inventory
3. Customers
4. Reports

Place secondary features such as settings, transactions, expenses, backup, and business profile under appropriate screens or a More menu.

Navigation must work correctly, preserve reasonable screen state, and support Android back navigation.

Use a navigation library compatible with the project's installed Android dependencies.

## 5. Screen-by-screen requirements

### A. Splash and onboarding

Create a simple branded splash screen with:
- Eta logo or a custom vector mark.
- App name and tagline.
- A subtle entrance animation.

On first launch, show a short onboarding flow that introduces sales, inventory, customer dues, and offline usage.

Allow the user to:
- Set up a business name.
- Select a business category.
- Choose a preferred currency, defaulting to INR.
- Enter an optional low-stock threshold.
- Skip optional setup steps.

Do not require account creation to use the local MVP.

### B. Home dashboard

This is the most important screen.

The dashboard should include:

**Header**
- Greeting.
- Business name.
- Current date.
- Clear offline/sync status indicator.
- Notifications or alerts shortcut.

**Today's overview**
- Sales total.
- Expenses total, if expense tracking is implemented.
- Outstanding customer dues.
- Number of transactions.
- Low-stock product count.

Make these figures readable at a glance. Do not place too many large statistic cards above the fold.

**Quick actions**
- Record Sale.
- Add Product.
- Record Payment.
- View Customers.

**Business insights**
- Daily sales trend.
- Best-selling items based on actual stored transactions.
- Products that need restocking.
- Recent transactions.

If there is insufficient data, show a helpful empty state and a button to record the first sale. Never invent business insights.

Include a prominent voice-entry shortcut, but allow every core operation to be completed manually.

### C. Sales and transaction recording

Implement a complete sales workflow.

Users must be able to:
- Select one or more products.
- Change quantities.
- See available stock.
- Calculate subtotal and final total.
- Select a customer or use a walk-in customer.
- Choose a payment method: cash, UPI, card, or other.
- Record a fully paid or partially paid sale.
- Review and confirm the sale.
- Save the transaction.
- View the saved receipt or transaction summary.

Validate quantities, prices, and payment amounts.

Prevent accidental duplicate transactions caused by repeated button taps.

**Inventory integration:** Saving a sale must reduce the appropriate product quantities. This change and the transaction record must be committed atomically in a Room database transaction.

If the sale cannot be saved, do not update the inventory independently.

Do not claim that a UPI payment has been verified. Selecting UPI only records the chosen payment method unless a genuine payment integration is implemented later.

### D. Voice-assisted sales

Create a dedicated voice-entry screen with:
- A microphone button.
- A recording/listening state.
- A transcription preview.
- A parsed action summary.
- A confirmation step.
- A manual editing option.
- A clear cancellation action.

Example command:

"Record a sale of two notebooks for Ravi."

Another example:

"Add ten packets of biscuits to inventory."

The app should convert recognized speech into a structured intent, validate it, and display the proposed action before changing business records.

Use Android speech recognition APIs through a dedicated abstraction layer. Handle permission denial, unavailable recognition services, silence, recognition errors, and network-dependent recognition.

**Important:** Speech recognition may require an internet connection depending on the device and installed recognition service. The rest of the application must remain usable offline.

For the initial version, implement a deterministic command parser for a limited set of supported phrases. Do not pretend a full conversational AI system exists if it has not been implemented.

Do not save or execute ambiguous commands without user confirmation.

If speech recognition is unavailable, provide a clear fallback to manual entry.

### E. Inventory management

Create an inventory screen with:
- Search.
- Category filters.
- Product list.
- Current stock.
- Selling price.
- Low-stock indicators.
- Add Product button.

Implement:
- Create product.
- View product details.
- Edit product.
- Delete product with confirmation.
- Update stock.
- Search and filter products.
- Sort by name, stock, or sales where supported.

Each product should have:
- Unique ID.
- Product name.
- Optional SKU or barcode.
- Category.
- Unit.
- Cost price.
- Selling price.
- Current quantity.
- Low-stock threshold.
- Creation and modification timestamps.

Validate all numeric fields and reject negative quantities and invalid prices.

When inventory changes manually, record the reason and quantity adjustment in stock history.

Create a low-stock screen that shows products below their configured thresholds.

Use an empty state when no products exist.

### F. Customers and outstanding dues

Create a customer management screen.

Implement:
- Add customer.
- Edit customer.
- Search customers.
- View customer details.
- View transaction history.
- View outstanding balance.
- Record customer payments.
- Track partial payments.
- Generate payment reminder text for manual sharing.

Customer fields:
- ID.
- Name.
- Optional phone number.
- Optional notes.
- Current outstanding balance, derived from the ledger.
- Created and modified timestamps.

Support walk-in customers without requiring a customer profile.

**Ledger integrity:** Customer balances must be derived from recorded sales, payments, refunds, and applicable adjustments. Avoid maintaining an independently editable balance that can become inconsistent.

A payment must update the relevant transaction or customer ledger in a database transaction.

Do not mark an outstanding amount as paid unless a payment record has actually been saved.

Allow the user to share a reminder using Android's native sharing interface. Do not send messages automatically.

### G. Transactions and receipts

Create a transaction history screen with:
- Search and filters.
- Date filtering.
- Payment status.
- Transaction total.
- Customer name when available.
- Payment method.

Users should be able to open a transaction and see:
- Items sold.
- Quantities and prices.
- Total amount.
- Amount paid.
- Remaining balance.
- Timestamp.
- Payment history.

Provide a readable receipt layout that can be shared or saved using appropriate Android APIs.

Implement refunds or sale cancellation carefully. Restore inventory and reverse the relevant ledger entries consistently. Do not silently delete financial history.

### H. Reports and analytics

Create a reports screen with selectable periods:
- Today.
- Last 7 days.
- Last 30 days.
- Custom date range, if feasible.

Show:
- Sales totals.
- Recorded expenses.
- Gross profit estimate when cost data is available.
- Outstanding dues.
- Transaction count.
- Best-selling products.
- Sales trend chart.

Use a lightweight chart library compatible with Compose, or create simple accessible charts using Compose primitives.

All calculations must use real stored records and correct date boundaries.

Clearly distinguish sales revenue, collected payments, expenses, outstanding dues, and profit estimates.

Do not treat all sales as cash collected. Do not label revenue as profit.

If historical comparison data is unavailable, display an honest empty or unavailable state.

### I. Expenses

If feasible after the primary workflows are stable, implement basic expense tracking:
- Add expense.
- Amount.
- Category.
- Date.
- Optional description.
- Expense history.

Expenses should be stored locally and included in reports.

Keep expenses separate from customer payments and sales revenue.

### J. Settings and business profile

Provide:
- Business name and profile.
- Currency preference.
- Low-stock preferences.
- Theme selection.
- Voice input settings.
- Data export.
- Backup and restore options.
- Privacy information.
- App version.
- Clear local data with an explicit confirmation.

If currency switching is supported, handle it as a display and formatting preference for new operations. Never silently reinterpret existing transaction amounts using a different currency.

## 6. Offline-first requirements

Offline functionality is a core product requirement.

Use Room as the source of truth for business records.

The following operations must work without internet:
- Opening the dashboard.
- Adding and editing products.
- Searching inventory.
- Recording sales.
- Updating stock.
- Adding customers.
- Recording customer payments.
- Viewing transaction history.
- Generating local reports.
- Viewing low-stock alerts.

Implement a repository layer so that UI components do not directly access the database.

Use Kotlin Coroutines and Flow to observe changes and update the UI reactively.

Display clear connectivity and sync states:
- Offline.
- Local data saved.
- Sync pending.
- Syncing.
- Sync complete.
- Sync failed.

Do not confuse device connectivity with successful server synchronization.

For the local-only MVP, use an explicit local-only mode and clearly state that data is stored on the device.

Design a future-compatible synchronization interface, but do not create a fake cloud sync indicator.

If cloud sync is implemented, use a real backend, authenticated users, stable record IDs, timestamps, retry handling, and a defined conflict-resolution strategy. Protect customer information and financial records.

Cloud synchronization must never silently overwrite newer business records.

## 7. Kotlin architecture

Use a maintainable, pragmatic architecture.

Recommended structure:

app/
  src/main/
    java/<package-name>/
      EtaApplication.kt
      MainActivity.kt
      navigation/
        EtaNavGraph.kt
        EtaDestination.kt
      core/
        ui/
          theme/
          components/
        util/
        validation/
      data/
        local/
          EtaDatabase.kt
          dao/
          entity/
        repository/
        preferences/
      domain/
        model/
        usecase/
      feature/
        onboarding/
        dashboard/
        sales/
        voice/
        inventory/
        customers/
        transactions/
        reports/
        expenses/
        settings/

Adapt package names and files to the existing project rather than blindly duplicating this tree.

Use:
- Kotlin.
- Jetpack Compose.
- Material 3.
- ViewModel.
- StateFlow or another appropriate observable state mechanism.
- Kotlin Coroutines and Flow.
- Room.
- Navigation Compose.
- DataStore for suitable user preferences.
- WorkManager only when there are actual background tasks to schedule.
- Dependency injection only if it simplifies the project.

Follow unidirectional data flow:
UI event → ViewModel → use case/repository → database → observable state → UI.

Represent loading, success, empty, and error states explicitly.

Keep composable functions small and reusable. Avoid giant screen files and excessive abstraction.

Use immutable UI state where practical.

## 8. Data model and integrity

Create appropriate Room entities and DAOs for:
- Product.
- Customer.
- Sale.
- SaleItem.
- Payment.
- Expense.
- StockMovement.

Additional entities can be introduced where they provide real value.

Use appropriate foreign keys and indexes.

Use stable identifiers, correct decimal monetary representation, and consistent timestamp handling.

Avoid floating-point arithmetic for currency. Use integer minor units or an appropriate decimal representation with explicit conversion and rounding rules.

Use database transactions for operations that affect multiple records.

Enforce:
- A sale cannot silently create negative inventory.
- Invalid payment amounts are rejected.
- Duplicate submissions do not create duplicate sales.
- Customer balances remain consistent with ledger entries.
- Stock history reflects stock changes.
- Deleting a product does not corrupt historical sale records.

Handle migrations properly as the schema evolves.

## 9. AI and smart features

Do not add AI merely to label the product as AI-powered.

The first version should provide useful automation through:
- Voice-to-command parsing.
- Low-stock insights.
- Daily sales summaries.
- Best-selling item identification.
- Simple natural-language search where practical.

AI-generated insights must be grounded in actual database data.

Use a defined structured output for AI-assisted commands. Validate product names, quantities, prices, customer identities, and permitted operations before execution.

Never allow a model response to directly execute arbitrary database operations.

Require user confirmation for financial transactions and other consequential actions.

Keep the core application usable when all AI or speech services are unavailable.

Treat cloud AI as an optional future enhancement, with explicit disclosure of what data would leave the device.

## 10. UI interaction quality

Every screen must include appropriate:
- Loading states.
- Empty states.
- Validation messages.
- Success feedback.
- Error feedback.
- Confirmation dialogs for destructive actions.
- Back navigation.
- Keyboard handling.
- Scrolling behavior.
- Accessibility labels.

Avoid:
- Placeholder buttons with no action.
- Hardcoded dashboard totals.
- Repeated sample records after every launch.
- Fake AI responses.
- Fake cloud synchronization.
- Unnecessary permission requests.
- Excessive popups.
- Tiny text.
- Overcrowded dashboards.

Seed a small, clearly identified demo dataset only when the user explicitly chooses demo mode. Never mix demo data with real business records.

## 11. Testing requirements

Write meaningful tests for critical business logic.

At minimum, test:
1. Product creation and editing.
2. Sale total calculations.
3. Stock reduction after a sale.
4. Prevention of negative stock.
5. Partial payment handling.
6. Customer balance calculations.
7. Stock restoration during refunds or cancellation.
8. Report totals over date ranges.
9. Database transaction consistency.
10. Voice-command parsing and invalid command handling.

Add Room DAO tests and ViewModel tests where feasible.

Build the app and resolve compilation errors, missing dependencies, navigation errors, and obvious runtime issues.

Do not claim tests passed unless they were actually run.

## 12. Implementation plan

Work in phases.

**Phase 1 — Foundation**
- Inspect the environment.
- Configure Kotlin, Compose, Material 3, and navigation.
- Create the theme and reusable UI components.
- Set up Room and repositories.
- Implement onboarding and the dashboard shell.

**Phase 2 — Core business workflows**
- Inventory CRUD.
- Sales creation and transaction history.
- Stock adjustments.
- Customers and payment ledger.
- Correct database transaction handling.

**Phase 3 — Business intelligence**
- Dashboard metrics.
- Low-stock alerts.
- Daily and weekly reports.
- Expense tracking if feasible.

**Phase 4 — Smart interaction**
- Voice recognition.
- Deterministic intent parser.
- Confirmation flow.
- AI-assisted features only where useful.

**Phase 5 — Quality**
- Offline testing.
- UI polish.
- Validation and error handling.
- Unit and database tests.
- Debug build.
- Fix compilation and runtime issues.

Do not stop after generating the project structure or the first screen. Continue until the core workflows are connected and functional.

If time or environment limitations prevent completion, finish the highest-priority working flows first and clearly list what remains.

## 13. Acceptance criteria

Eta's MVP is ready when:
- The application launches successfully.
- Onboarding completes.
- Products can be created, edited, searched, and persisted.
- Sales can be recorded and viewed.
- A sale updates stock correctly.
- Customer payments and outstanding balances are consistent.
- Dashboard and reports reflect stored data.
- Data survives application restarts.
- Core workflows work in airplane mode.
- Voice entry handles permissions and errors gracefully.
- The interface is visually consistent and mobile-friendly.
- Critical business logic has automated tests.
- The project can produce a debug APK using the available build environment.

## 14. Final instructions

Start by inspecting the current workspace, available SDK, Gradle setup, and existing files.

Then create a short implementation plan and proceed to implementation without waiting for unnecessary approval.

Prefer stable, compatible dependency versions. Do not upgrade the entire toolchain without a reason.

Run the available build and tests, fix issues, and provide a concise completion report containing:
- Implemented features.
- Project structure.
- Architecture and dependencies.
- Build and test results.
- Any incomplete features.
- Exact steps to run the app.
- Any remaining setup requirements.

**Final product goal:** Eta should look like a premium, modern Android business app while remaining simple enough for a small shop owner to use confidently. It must be genuinely offline-capable, reliable with financial records, and easy to extend into a monetizable product.