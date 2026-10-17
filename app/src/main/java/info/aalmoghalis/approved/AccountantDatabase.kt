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

data class BusinessDocument(
    val id: Long,
    val kind: String,
    val partyName: String,
    val amount: Double,
    val notes: String,
    val createdAt: Long
)

class AccountantDatabase(context: Context) :
    SQLiteOpenHelper(context, "approved_accounting.db", null, 2) {

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
                kind TEXT NOT NULL CHECK(kind IN ('SALE','PURCHASE')),
                party_name TEXT NOT NULL,
                amount REAL NOT NULL CHECK(amount >= 0),
                notes TEXT NOT NULL DEFAULT '',
                created_at INTEGER NOT NULL
            )""".trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_documents_kind_date ON documents(kind, created_at DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createDocumentsTable(db)
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
            "SELECT id, kind, party_name, amount, notes, created_at FROM documents WHERE kind=? ORDER BY created_at DESC, id DESC",
            arrayOf(kind)
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        BusinessDocument(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getDouble(3),
                            cursor.getString(4),
                            cursor.getLong(5)
                        )
                    )
                }
            }
        }
    }

    fun addDocument(kind: String, partyName: String, amount: Double, notes: String) {
        require(kind == "SALE" || kind == "PURCHASE")
        require(partyName.isNotBlank() && amount >= 0.0 && amount.isFinite())
        val values = ContentValues().apply {
            put("kind", kind)
            put("party_name", partyName.trim())
            put("amount", amount)
            put("notes", notes.trim())
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insertOrThrow("documents", null, values)
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