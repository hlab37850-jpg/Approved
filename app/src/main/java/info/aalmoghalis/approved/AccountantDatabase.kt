package info.aalmoghalis.approved

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class LedgerRecord(
    val id: Long,
    val name: String,
    val detail: String,
    val quantity: Double = 0.0
)

data class DocumentLineDraft(val itemName: String, val quantity: Double, val unitPrice: Double)

data class DocumentLine(
    val id: Long,
    val documentId: Long,
    val itemName: String,
    val quantity: Double,
    val unitPrice: Double,
    val lineTotal: Double
)

data class DocumentTotals(val count: Int, val total: Double)

data class FinancialSummary(
    val allSales: DocumentTotals,
    val allPurchases: DocumentTotals,
    val monthSales: DocumentTotals,
    val monthPurchases: DocumentTotals
)

data class BusinessDocument(
    val id: Long,
    val documentNo: String,
    val kind: String,
    val partyName: String,
    val amount: Double,
    val notes: String,
    val createdAt: Long
)

class AccountantDatabase(context: Context) :
    SQLiteOpenHelper(context, "approved_accounting.db", null, 4) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createBaseTables(db)
        createDocumentsTable(db)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_documents_number ON documents(document_no)")
    }

    private fun createBaseTables(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS customers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT NOT NULL DEFAULT '',
                balance REAL NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                sku TEXT NOT NULL DEFAULT '',
                quantity REAL NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS company_settings (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                company_name TEXT NOT NULL DEFAULT '',
                phone TEXT NOT NULL DEFAULT '',
                address TEXT NOT NULL DEFAULT '',
                currency TEXT NOT NULL DEFAULT 'ر.ي'
            )""".trimIndent()
        )
        db.execSQL("INSERT OR IGNORE INTO company_settings (id) VALUES (1)")
    }

    private fun createDocumentsTable(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS documents (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                document_no TEXT NOT NULL DEFAULT '',
                kind TEXT NOT NULL CHECK(kind IN ('SALE','PURCHASE')),
                party_name TEXT NOT NULL,
                amount REAL NOT NULL CHECK(amount >= 0),
                notes TEXT NOT NULL DEFAULT '',
                created_at INTEGER NOT NULL,
                stock_posted INTEGER NOT NULL DEFAULT 0 CHECK(stock_posted IN (0,1))
            )""".trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_documents_kind_date ON documents(kind, created_at DESC)")
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS document_lines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                document_id INTEGER NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
                item_name TEXT NOT NULL,
                quantity REAL NOT NULL CHECK(quantity > 0),
                unit_price REAL NOT NULL CHECK(unit_price >= 0),
                line_total REAL NOT NULL CHECK(line_total >= 0)
            )""".trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_document_lines_document ON document_lines(document_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createDocumentsTable(db)
        if (oldVersion < 3) {
            if (!hasColumn(db, "documents", "document_no")) {
                db.execSQL("ALTER TABLE documents ADD COLUMN document_no TEXT NOT NULL DEFAULT ''")
            }
            db.execSQL("UPDATE documents SET document_no = CASE WHEN kind='SALE' THEN 'SAL-' ELSE 'PUR-' END || printf('%06d', id) WHERE document_no = ''")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_documents_number ON documents(document_no)")
            createDocumentLinesTable(db)
        }
        if (oldVersion < 4 && !hasColumn(db, "documents", "stock_posted")) {
            db.execSQL("ALTER TABLE documents ADD COLUMN stock_posted INTEGER NOT NULL DEFAULT 0")
        }
    }

    private fun hasColumn(db: SQLiteDatabase, table: String, column: String): Boolean =
        db.rawQuery("PRAGMA table_info($table)", null).use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            var found = false
            while (cursor.moveToNext()) {
                if (nameIndex >= 0 && cursor.getString(nameIndex) == column) found = true
            }
            found
        }

    private fun createDocumentLinesTable(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS document_lines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                document_id INTEGER NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
                item_name TEXT NOT NULL,
                quantity REAL NOT NULL CHECK(quantity > 0),
                unit_price REAL NOT NULL CHECK(unit_price >= 0),
                line_total REAL NOT NULL CHECK(line_total >= 0)
            )""".trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_document_lines_document ON document_lines(document_id)")
    }

    fun listCustomers(): List<LedgerRecord> = readableDatabase.rawQuery(
        "SELECT id, name, phone, balance FROM customers ORDER BY name COLLATE NOCASE", null
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                val balance = cursor.getDouble(3)
                add(LedgerRecord(cursor.getLong(0), cursor.getString(1), cursor.getString(2) + " • الرصيد: " + balance))
            }
        }
    }

    fun addCustomer(name: String, phone: String) {
        val values = ContentValues().apply {
            put("name", name.trim())
            put("phone", phone.trim())
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insertOrThrow("customers", null, values)
    }

    fun listItems(): List<LedgerRecord> = readableDatabase.rawQuery(
        "SELECT id, name, sku, quantity FROM items ORDER BY name COLLATE NOCASE", null
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(LedgerRecord(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getDouble(3)))
            }
        }
    }

    fun addItem(name: String, sku: String, quantity: Double) {
        val values = ContentValues().apply {
            put("name", name.trim())
            put("sku", sku.trim())
            put("quantity", quantity)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insertOrThrow("items", null, values)
    }

    fun listDocuments(kind: String): List<BusinessDocument> {
        require(kind == "SALE" || kind == "PURCHASE")
        return readableDatabase.rawQuery(
            "SELECT id, document_no, kind, party_name, amount, notes, created_at FROM documents WHERE kind=? ORDER BY created_at DESC, id DESC",
            arrayOf(kind)
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        BusinessDocument(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getDouble(4),
                            cursor.getString(5),
                            cursor.getLong(6)
                        )
                    )
                }
            }
        }
    }

    fun financialSummary(monthStart: Long): FinancialSummary {
        fun totals(kind: String, since: Long? = null): DocumentTotals {
            val sql = if (since == null) {
                "SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM documents WHERE kind=?"
            } else {
                "SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM documents WHERE kind=? AND created_at>=?"
            }
            val args = if (since == null) arrayOf(kind) else arrayOf(kind, since.toString())
            return readableDatabase.rawQuery(sql, args).use { cursor ->
                if (cursor.moveToFirst()) DocumentTotals(cursor.getInt(0), cursor.getDouble(1))
                else DocumentTotals(0, 0.0)
            }
        }
        return FinancialSummary(
            allSales = totals("SALE"),
            allPurchases = totals("PURCHASE"),
            monthSales = totals("SALE", monthStart),
            monthPurchases = totals("PURCHASE", monthStart)
        )
    }

    fun addDocumentWithLines(
        kind: String,
        partyName: String,
        notes: String,
        lines: List<DocumentLineDraft>
    ) {
        require(kind == "SALE" || kind == "PURCHASE")
        require(partyName.isNotBlank() && lines.isNotEmpty())
        require(lines.all {
            it.itemName.isNotBlank() && it.quantity.isFinite() && it.quantity > 0.0 &&
                it.unitPrice.isFinite() && it.unitPrice >= 0.0 &&
                (it.quantity * it.unitPrice).isFinite()
        })
        val total = lines.sumOf { it.quantity * it.unitPrice }
        require(total.isFinite() && total >= 0.0)
        val grouped = lines.groupBy { it.itemName.trim().lowercase(java.util.Locale.ROOT) }
        val db = writableDatabase
        db.beginTransaction()
        try {
            // All lines must refer to a registered inventory item. This keeps stock traceable.
            val stockChanges = grouped.map { (_, sameNameLines) ->
                val name = sameNameLines.first().itemName.trim()
                val quantity = sameNameLines.sumOf { it.quantity }
                require(quantity.isFinite() && quantity > 0.0) { "كمية الصنف غير صالحة: $name" }
                val item = db.rawQuery(
                    "SELECT id, quantity FROM items WHERE name=? COLLATE NOCASE ORDER BY id LIMIT 1",
                    arrayOf(name)
                ).use { cursor ->
                    if (cursor.moveToFirst()) Pair(cursor.getLong(0), cursor.getDouble(1)) else null
                } ?: throw IllegalArgumentException("الصنف «$name» غير مسجل في المخزون. أضفه أولاً من قسم الأصناف.")
                if (kind == "SALE") {
                    require(item.second >= quantity) {
                        "الكمية المتاحة من «$name» هي ${item.second} فقط، والكمية المطلوبة ${quantity}."
                    }
                }
                Triple(item.first, quantity, name)
            }
            val values = ContentValues().apply {
                put("document_no", "")
                put("kind", kind)
                put("party_name", partyName.trim())
                put("amount", total)
                put("notes", notes.trim())
                put("created_at", System.currentTimeMillis())
                put("stock_posted", 0)
            }
            val id = db.insertOrThrow("documents", null, values)
            lines.forEach { line ->
                val lineValues = ContentValues().apply {
                    put("document_id", id)
                    put("item_name", line.itemName.trim())
                    put("quantity", line.quantity)
                    put("unit_price", line.unitPrice)
                    put("line_total", line.quantity * line.unitPrice)
                }
                db.insertOrThrow("document_lines", null, lineValues)
            }
            stockChanges.forEach { (itemId, quantity, name) ->
                val delta = if (kind == "SALE") -quantity else quantity
                db.execSQL("UPDATE items SET quantity=quantity+? WHERE id=?", arrayOf(delta, itemId))
            }
            val prefix = if (kind == "SALE") "SAL-" else "PUR-"
            db.execSQL("UPDATE documents SET document_no=?, stock_posted=1 WHERE id=?",
                arrayOf(prefix + id.toString().padStart(6, '0'), id))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun listDocumentLines(documentId: Long): List<DocumentLine> =
        readableDatabase.rawQuery(
            "SELECT id, document_id, item_name, quantity, unit_price, line_total FROM document_lines WHERE document_id=? ORDER BY id",
            arrayOf(documentId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        DocumentLine(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getDouble(3),
                            cursor.getDouble(4),
                            cursor.getDouble(5)
                        )
                    )
                }
            }
        }

    fun addDocument(kind: String, partyName: String, amount: Double, notes: String) {
        require(kind == "SALE" || kind == "PURCHASE")
        require(partyName.isNotBlank() && amount >= 0.0 && amount.isFinite())
        val db = writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("document_no", "")
                put("kind", kind)
                put("party_name", partyName.trim())
                put("amount", amount)
                put("notes", notes.trim())
                put("created_at", System.currentTimeMillis())
            }
            val id = db.insertOrThrow("documents", null, values)
            val prefix = if (kind == "SALE") "SAL-" else "PUR-"
            db.execSQL(
                "UPDATE documents SET document_no=? WHERE id=?",
                arrayOf(prefix + id.toString().padStart(6, '0'), id)
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteRecord(table: String, id: Long) {
        require(table == "customers" || table == "items" || table == "documents") { "Unsupported table" }
        val db = writableDatabase
        if (table != "documents") {
            db.delete(table, "id = ?", arrayOf(id.toString()))
            return
        }
        db.beginTransaction()
        try {
            val doc = db.rawQuery(
                "SELECT kind, stock_posted FROM documents WHERE id=?",
                arrayOf(id.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) Pair(cursor.getString(0), cursor.getInt(1) == 1) else null
            }
            if (doc != null && doc.second) {
                val lines = db.rawQuery(
                    "SELECT item_name, SUM(quantity) FROM document_lines WHERE document_id=? GROUP BY item_name COLLATE NOCASE",
                    arrayOf(id.toString())
                ).use { cursor ->
                    buildList {
                        while (cursor.moveToNext()) add(Pair(cursor.getString(0), cursor.getDouble(1)))
                    }
                }
                lines.forEach { (name, quantity) ->
                    val itemId = db.rawQuery(
                        "SELECT id FROM items WHERE name=? COLLATE NOCASE ORDER BY id LIMIT 1",
                        arrayOf(name)
                    ).use { cursor -> if (cursor.moveToFirst()) cursor.getLong(0) else null }
                    if (itemId != null) {
                        val delta = if (doc.first == "SALE") quantity else -quantity
                        db.execSQL("UPDATE items SET quantity=quantity+? WHERE id=?", arrayOf(delta, itemId))
                    }
                }
            }
            db.delete("documents", "id = ?", arrayOf(id.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun companyName(): String = readableDatabase.rawQuery(
        "SELECT company_name FROM company_settings WHERE id=1", null
    ).use { if (it.moveToFirst()) it.getString(0) ?: "" else "" }

    fun saveCompanyName(name: String) {
        writableDatabase.execSQL(
            "UPDATE company_settings SET company_name=? WHERE id=1",
            arrayOf(name.trim())
        )
    }
}