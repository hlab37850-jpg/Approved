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

class AccountantDatabase(context: Context) :
    SQLiteOpenHelper(context, "approved_accounting.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE customers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT NOT NULL DEFAULT '',
                balance REAL NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                sku TEXT NOT NULL DEFAULT '',
                quantity REAL NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE company_settings (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                company_name TEXT NOT NULL DEFAULT '',
                phone TEXT NOT NULL DEFAULT '',
                address TEXT NOT NULL DEFAULT '',
                currency TEXT NOT NULL DEFAULT 'ر.ي'
            )""".trimIndent()
        )
        db.execSQL("INSERT INTO company_settings (id) VALUES (1)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future schema changes must use explicit migrations; user data is never dropped.
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

    fun deleteRecord(table: String, id: Long) {
        require(table == "customers" || table == "items") { "Unsupported table" }
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
