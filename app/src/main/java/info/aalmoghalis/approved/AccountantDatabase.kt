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
    SQLiteOpenHelper(context, "approved_accounting.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        createBaseTables(db)
        createDocumentsTable(db)
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
                created_at INTEGER NOT NULL
            )""".trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_documents_kind_date ON documents(kind, created_at DESC)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_documents_number ON documents(document_no)")
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
            db.execSQL("ALTER TABLE documents ADD COLUMN document_no TEXT NOT NULL DEFAULT ''")
            db.execSQL("UPDATE documents SET document_no = CASE WHEN kind='SALE' THEN 'SAL-' ELSE 'PUR-' END || printf('%06d', id) WHERE document_no = ''")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_documents_number ON documents(document_no)")
            createDocumentLinesTable(db)
        }
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
        writableDatabase.delete(table, "id = ?", arrayOf(id.toString()))
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