package info.aalmoghalis.approved

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccountantDatabaseMigrationTest {
    @Test
    fun versionThreeDatabaseUpgradesWithoutLosingExistingDocuments() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("approved_accounting.db")

        context.openOrCreateDatabase("approved_accounting.db", Context.MODE_PRIVATE, null).use { oldDb ->
            oldDb.execSQL(
                """CREATE TABLE documents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    document_no TEXT NOT NULL DEFAULT '',
                    kind TEXT NOT NULL,
                    party_name TEXT NOT NULL,
                    amount REAL NOT NULL,
                    notes TEXT NOT NULL DEFAULT '',
                    created_at INTEGER NOT NULL
                )""".trimIndent()
            )
            oldDb.execSQL(
                "INSERT INTO documents(document_no, kind, party_name, amount, notes, created_at) VALUES(?,?,?,?,?,?)",
                arrayOf("SAL-000042", "SALE", "عميل سابق", 123.5, "سجل قديم", 1700000000000L)
            )
            oldDb.version = 3
        }

        val upgraded = AccountantDatabase(context)
        try {
            val db = upgraded.writableDatabase
            assertEquals(4, db.version)
            db.rawQuery("PRAGMA table_info(documents)", null).use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                var foundStockPosted = false
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == "stock_posted") foundStockPosted = true
                }
                assertTrue("Migration should add stock_posted", foundStockPosted)
            }
            val saved = upgraded.listDocuments("SALE").single()
            assertEquals("SAL-000042", saved.documentNo)
            assertEquals("عميل سابق", saved.partyName)
            assertEquals(123.5, saved.amount, 0.000001)
            assertEquals("سجل قديم", saved.notes)
        } finally {
            upgraded.close()
            context.deleteDatabase("approved_accounting.db")
        }
    }
}
