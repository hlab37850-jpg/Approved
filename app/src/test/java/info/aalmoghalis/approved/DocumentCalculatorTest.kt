package info.aalmoghalis.approved

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DocumentCalculatorTest {
    @Test
    fun calculatesSumOfQuantityTimesUnitPrice() {
        val total = DocumentCalculator.total(
            listOf(
                DocumentLineDraft("أسمنت", 3.0, 1200.0),
                DocumentLineDraft("حديد", 2.0, 2500.0)
            )
        )
        assertEquals(8600.0, total, 0.000001)
    }

    @Test
    fun rejectsEmptyLines() {
        assertThrows(IllegalArgumentException::class.java) {
            DocumentCalculator.total(emptyList())
        }
    }

    @Test
    fun rejectsZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException::class.java) {
            DocumentCalculator.total(listOf(DocumentLineDraft("صنف", 0.0, 100.0)))
        }
    }

    @Test
    fun rejectsNonFiniteLineTotal() {
        assertThrows(IllegalArgumentException::class.java) {
            DocumentCalculator.total(listOf(DocumentLineDraft("صنف", Double.MAX_VALUE, 2.0)))
        }
    }

    @Test
    fun acceptsZeroPriceButNotNegativePrice() {
        assertEquals(
            0.0,
            DocumentCalculator.total(listOf(DocumentLineDraft("خدمة مجانية", 1.0, 0.0))),
            0.0
        )
        assertThrows(IllegalArgumentException::class.java) {
            DocumentCalculator.total(listOf(DocumentLineDraft("صنف", 1.0, -0.01)))
        }
    }
}
