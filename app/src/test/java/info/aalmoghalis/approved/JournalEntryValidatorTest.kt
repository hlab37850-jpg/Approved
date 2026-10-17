package info.aalmoghalis.approved

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class JournalEntryValidatorTest {
    @Test
    fun acceptsBalancedTwoSidedEntry() {
        val totals = JournalEntryValidator.validate(
            listOf(
                JournalLineDraft("AR", debit = 250.0),
                JournalLineDraft("SALES", credit = 250.0)
            )
        )
        assertEquals(250.0, totals.debit, 0.000001)
        assertEquals(250.0, totals.credit, 0.000001)
    }

    @Test
    fun rejectsUnbalancedEntry() {
        assertThrows(IllegalArgumentException::class.java) {
            JournalEntryValidator.validate(
                listOf(
                    JournalLineDraft("AR", debit = 250.0),
                    JournalLineDraft("SALES", credit = 249.0)
                )
            )
        }
    }

    @Test
    fun rejectsLineWithBothDebitAndCredit() {
        assertThrows(IllegalArgumentException::class.java) {
            JournalEntryValidator.validate(
                listOf(
                    JournalLineDraft("AR", debit = 10.0, credit = 10.0),
                    JournalLineDraft("SALES", credit = 10.0)
                )
            )
        }
    }

    @Test
    fun rejectsMissingAccountNegativeAndNonFiniteAmounts() {
        val invalidEntries = listOf(
            listOf(JournalLineDraft("", debit = 1.0), JournalLineDraft("SALES", credit = 1.0)),
            listOf(JournalLineDraft("AR", debit = -1.0), JournalLineDraft("SALES", credit = 1.0)),
            listOf(JournalLineDraft("AR", debit = Double.NaN), JournalLineDraft("SALES", credit = 1.0)),
            listOf(JournalLineDraft("AR", debit = Double.POSITIVE_INFINITY), JournalLineDraft("SALES", credit = 1.0))
        )
        invalidEntries.forEach { entry ->
            assertThrows(IllegalArgumentException::class.java) {
                JournalEntryValidator.validate(entry)
            }
        }
    }

    @Test
    fun rejectsSingleLineAndZeroValueEntry() {
        assertThrows(IllegalArgumentException::class.java) {
            JournalEntryValidator.validate(listOf(JournalLineDraft("AR", debit = 1.0)))
        }
        assertThrows(IllegalArgumentException::class.java) {
            JournalEntryValidator.validate(
                listOf(JournalLineDraft("AR"), JournalLineDraft("SALES"))
            )
        }
    }
}
