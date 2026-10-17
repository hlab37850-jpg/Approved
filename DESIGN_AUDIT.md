# Design audit — reference repository

Reference: https://github.com/hlab37850-jpg/almahaseb-pro-apk-analysis

## Confirmed from the decompiled repository
- Android native application package: `info.aalmoghalis.inventory`; manifest version `Inv1.170`.
- min SDK 21, target SDK 35, compile SDK 35.
- Launcher activity: `info.aalmoghalis.inventory.activity.FragmentStatePagerSupport_Main`.
- The launcher uses an AndroidX `Toolbar`, `DrawerLayout`, `ListView`, `ViewPager`, and SQLite access.
- Verified colors: `colorPrimary #0a6ea9`, `colorPrimaryDark #1b5088`, and `colorAccent #ff4081`.
- Main layout uses CoordinatorLayout, AppBarLayout, Toolbar and a floating action button.
- Resource inventory includes customers, item details, transaction screens, reports, settings, printing, barcode, PDF/Excel, backup/restore, messaging, sales and purchases.

## Implemented in Approved
- Kotlin + Jetpack Compose Arabic RTL foundation, using the verified primary colors.
- SQLite database with versioned schema and a non-destructive migration adding the documents table.
- Customer records: add, list and delete.
- Inventory items: add, list and delete, including SKU and quantity fields.
- Company name setting: save locally.
- Sales and purchase document registers: save, list and delete party name, amount, notes and timestamp.
- Empty states explain missing data; the app does not seed sample business records.

## Important limits
- Sales/purchase records are currently registers only. They do not generate journal entries, update customer/vendor balances, deduct/add stock, or represent a complete tax invoice workflow.
- General ledger, accounts tree, customer statements, full reports, backups, barcode, printing, imports and all source navigation remain unfinished.
- The Compose screen is not yet a pixel-perfect reconstruction of every source screen; no matching screenshot set was supplied for pixel comparison.

## Next work
1. Verify the latest GitHub Actions build and APK artifact.
2. Map source navigation and app-owned layouts/screens systematically.
3. Implement transaction line items, document numbering, totals and validation.
4. Implement accounting posting rules only after defining and testing the double-entry model.
5. Add tests for database migration, record persistence and financial validation.
