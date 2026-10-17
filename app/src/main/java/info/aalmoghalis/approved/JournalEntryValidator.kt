package info.aalmoghalis.approved

data class JournalLineDraft(
    val accountCode: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0
)

data class JournalTotals(
    val debit: Double,
    val credit: Double
)

/**
 * Validates a proposed double-entry journal before it can be persisted.
 * This is deliberately a pure validator; database posting must call it inside
 * the same transaction as the source document and stock movements.
 */
object JournalEntryValidator {
    private const val BALANCE_TOLERANCE = 0.000001

    fun validate(lines: List<JournalLineDraft>): JournalTotals {
        require(lines.size >= 2) { "يجب أن يحتوي القيد على سطرين على الأقل." }
        require(lines.all { line ->
            line.accountCode.isNotBlank() &&
                line.debit.isFinite() && line.credit.isFinite() &&
                line.debit >= 0.0 && line.credit >= 0.0 &&
                ((line.debit > 0.0 && line.credit == 0.0) ||
                    (line.credit > 0.0 && line.debit == 0.0))
        }) { "كل سطر يجب أن يحتوي حساباً ومبلغاً موجباً في طرف واحد فقط." }

        val debit = lines.sumOf { it.debit }
        val credit = lines.sumOf { it.credit }
        require(debit.isFinite() && credit.isFinite()) { "إجمالي القيد خارج النطاق المسموح." }
        require(debit > 0.0 && credit > 0.0) { "يجب أن يحتوي القيد على مبالغ مدينة ودائنة." }
        require(kotlin.math.abs(debit - credit) <= BALANCE_TOLERANCE) {
            "القيد غير متوازن: المدين $debit والدائن $credit."
        }
        return JournalTotals(debit, credit)
    }
}
