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
- Sales and purchase document registers: save, list and delete party name, amount, notes and timestamp; newly created records receive unique SAL-/PUR- numbers.
- Version 3 database migration backfills numbers for older records and adds a document-lines table without deleting existing records.
- Reports screen calculates sales/purchase totals and record counts for the current month and all saved history directly from SQLite.
- Version 4 database migration adds a stock-posting marker while preserving old records; old documents are not retroactively applied to stock.
- New sales/purchase documents require registered inventory items, validate available stock for sales, update stock in the same SQLite transaction, and reverse the movement when a document is deleted where safe.
- Deleting a stock item referenced by posted documents is blocked; deleting a purchase is blocked when reversing it would make the current stock negative.
- A pure document-total calculator now has unit tests for totals, empty lines, invalid quantities, negative prices and overflow; CI runs unit tests before assembling the APK.
- Empty states explain missing data; the app does not seed sample business records.

## Important limits
- Sales and purchase entry now supports multiple line items with item name, quantity and unit price; the total is calculated from the lines and saved with them in one SQLite transaction. Saved line details appear in each document register.
- These documents now update item quantities, but still do not post double-entry journal entries or update customer/vendor balances, so they are not yet a complete accounting workflow or complete tax invoice workflow.
- General ledger, accounts tree, customer statements, profit calculation, backups, barcode, printing, imports and all source navigation remain unfinished. The reports screen is a real summary of document registers, not a complete financial reporting system.
- The Compose screen is not yet a pixel-perfect reconstruction of every source screen; no matching screenshot set was supplied for pixel comparison.

## Next work
1. Verify the latest GitHub Actions build and APK artifact.
2. Map source navigation and app-owned layouts/screens systematically.
3. Add database migration and inventory-posting integration tests, including purchase reversal and repeated deletion cases.
4. Implement balanced double-entry posting rules and customer/vendor balances with explicit account mappings.
5. Add account tree, ledger, statements and tested financial reports.

