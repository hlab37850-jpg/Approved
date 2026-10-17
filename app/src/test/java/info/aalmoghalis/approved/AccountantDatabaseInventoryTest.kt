package info.aalmoghalis.approved

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccountantDatabaseInventoryTest {
    private lateinit var context: Context
    private lateinit var database: AccountantDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase("approved_accounting.db")
        database = AccountantDatabase(context)
        database.writableDatabase
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) database.close()
        context.deleteDatabase("approved_accounting.db")
    }

    @Test
    fun purchaseAddsStockAndDeletingPurchaseReversesIt() {
        database.addItem("أسمنت", "CEM-1", 10.0)
        database.addDocumentWithLines(
            "PURCHASE", "المورد", "",
            listOf(DocumentLineDraft("أسمنت", 5.0, 100.0))
        )
        assertEquals(15.0, database.listItems().single().quantity, 0.000001)

        val purchase = database.listDocuments("PURCHASE").single()
        database.deleteRecord("documents", purchase.id)
        assertEquals(10.0, database.listItems().single().quantity, 0.000001)
    }

    @Test
    fun saleReducesStockAndDeletingSaleRestoresIt() {
        database.addItem("حديد", "STL-1", 12.0)
        database.addDocumentWithLines(
            "SALE", "العميل", "",
            listOf(DocumentLineDraft("حديد", 4.0, 500.0))
        )
        assertEquals(8.0, database.listItems().single().quantity, 0.000001)

        val sale = database.listDocuments("SALE").single()
        database.deleteRecord("documents", sale.id)
        assertEquals(12.0, database.listItems().single().quantity, 0.000001)
    }

    @Test
    fun saleCannotExceedAvailableStockAndDoesNotCreatePartialDocument() {
        database.addItem("بلاط", "TIL-1", 2.0)
        assertThrows(IllegalArgumentException::class.java) {
            database.addDocumentWithLines(
                "SALE", "عميل", "",
                listOf(DocumentLineDraft("بلاط", 3.0, 250.0))
            )
        }
        assertEquals(2.0, database.listItems().single().quantity, 0.000001)
        assertEquals(0, database.listDocuments("SALE").size)
    }

    @Test
    fun documentRequiresRegisteredInventoryItem() {
        assertThrows(IllegalArgumentException::class.java) {
            database.addDocumentWithLines(
                "PURCHASE", "مورد", "",
                listOf(DocumentLineDraft("صنف غير مسجل", 1.0, 50.0))
            )
        }
        assertEquals(0, database.listDocuments("PURCHASE").size)
    }

    @Test
    fun cannotDeletePurchaseIfStockWasAlreadyConsumed() {
        database.addItem("خشب", "WD-1", 0.0)
        database.addDocumentWithLines(
            "PURCHASE", "مورد", "",
            listOf(DocumentLineDraft("خشب", 3.0, 100.0))
        )
        database.addDocumentWithLines(
            "SALE", "عميل", "",
            listOf(DocumentLineDraft("خشب", 2.0, 150.0))
        )
        val purchase = database.listDocuments("PURCHASE").single()
        assertThrows(IllegalStateException::class.java) {
            database.deleteRecord("documents", purchase.id)
        }
        assertEquals(1.0, database.listItems().single().quantity, 0.000001)
        assertEquals(1, database.listDocuments("PURCHASE").size)
    }

    @Test
    fun cannotDeleteInventoryItemReferencedByPostedDocuments() {
        database.addItem("رمل", "SND-1", 5.0)
        database.addDocumentWithLines(
            "SALE", "عميل", "",
            listOf(DocumentLineDraft("رمل", 1.0, 200.0))
        )
        val item = database.listItems().single()
        assertThrows(IllegalStateException::class.java) {
            database.deleteRecord("items", item.id)
        }
        assertEquals(1, database.listItems().size)
    }
}
