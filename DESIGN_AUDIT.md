# Design audit — reference repository

Reference: https://github.com/hlab37850-jpg/almahaseb-pro-apk-analysis

## Confirmed from the decompiled repository
- Android native application package in manifest: `info.aalmoghalis.inventory`.
- Manifest version: `Inv1.170`; min SDK 21, target SDK 35, compile SDK 35.
- Launcher activity: `info.aalmoghalis.inventory.activity.FragmentStatePagerSupport_Main`.
- The launcher activity uses an AndroidX `Toolbar`, `DrawerLayout`, `ListView`, `ViewPager`, and SQLite database access.
- Reference resource colors include `colorPrimary #0a6ea9`, `colorPrimaryDark #1b5088`, and `colorAccent #ff4081`.
- Resources include icons for inventory/items, purchases, sales, reports, accounts, settings, backup/restore, barcode, PDF, Excel, printing, and messaging.
- The decompiled tree contains many application activities for customer/item details, transactions, reports, settings, printing, and account trees.

## Current implementation status
The new project currently uses Kotlin and Jetpack Compose as a maintainable native Android UI foundation. The first pass uses the verified primary color values and RTL Arabic UI. This is not yet a pixel-perfect reconstruction of every reference screen.

## Remaining work
1. Inspect the main activity's navigation/page setup and relevant app-owned resource definitions.
2. Build a screen inventory covering every declared app activity and navigation entry.
3. Reimplement the toolbar, drawer, tabs/pages, and screen-specific controls based on source evidence.
4. Recover/recreate suitable app-owned icons where legally and technically appropriate.
5. Add persistent database models and real workflows only after mapping the source behavior; do not show demo values as real data.
6. Compare screenshots against reference screenshots if available.
7. Verify cloud build status and APK artifact from GitHub Actions.
