# Design audit — reference repository

Reference: https://github.com/hlab37850-jpg/almahaseb-pro-apk-analysis

## Confirmed from the decompiled repository
- Android native application package in manifest: `info.aalmoghalis.inventory`.
- Manifest version: `Inv1.170`; min SDK 21, target SDK 35, compile SDK 35.
- Launcher activity: `info.aalmoghalis.inventory.activity.FragmentStatePagerSupport_Main`.
- The launcher activity uses an AndroidX `Toolbar`, `DrawerLayout`, `ListView`, `ViewPager`, and SQLite database access.
- Reference resource colors include `colorPrimary #0a6ea9`, `colorPrimaryDark #1b5088`, and `colorAccent #ff4081`.
- The main layout uses a CoordinatorLayout, AppBarLayout, Toolbar and a floating action button.
- Resources include icons for inventory/items, purchases, sales, reports, accounts, settings, backup/restore, barcode, PDF, Excel, printing, and messaging.
- The decompiled tree contains many application activities for customer/item details, transactions, reports, settings, printing, and account trees.

## Current implementation status
The new project uses Kotlin and Jetpack Compose as a maintainable native Android UI foundation. The first pass uses the verified primary color values and RTL Arabic UI. Local SQLite tables now exist for customers, inventory items, and company settings. The UI supports adding/listing/deleting customer and item records and saving the company name. The data is stored on-device and no sample records are seeded.

This remains a functional foundation, not a pixel-perfect reconstruction of every reference screen. Sales, purchases, ledger transactions, reporting, backups, barcode, printing, and complete reference navigation are not implemented yet.

## Remaining work
1. Inspect the main activity's navigation/page setup and relevant app-owned resource definitions.
2. Build a screen inventory covering every declared app activity and navigation entry.
3. Reimplement the toolbar, drawer, tabs/pages, and screen-specific controls based on source evidence.
4. Recover/recreate suitable app-owned icons where legally and technically appropriate.
5. Implement validated accounting workflows and database relations/migrations before enabling transaction features.
6. Compare screenshots against reference screenshots if available.
7. Verify the latest cloud build and APK artifact from GitHub Actions.
