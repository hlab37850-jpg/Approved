package info.aalmoghalis.approved

data class DocumentLineDraft(
    val itemName: String,
    val quantity: Double,
    val unitPrice: Double
)

object DocumentCalculator {
    fun total(lines: List<DocumentLineDraft>): Double {
        require(lines.isNotEmpty()) { "يجب إضافة بند واحد على الأقل." }
        require(lines.all {
            it.itemName.isNotBlank() &&
                it.quantity.isFinite() && it.quantity > 0.0 &&
                it.unitPrice.isFinite() && it.unitPrice >= 0.0 &&
                (it.quantity * it.unitPrice).isFinite()
        }) { "توجد بيانات غير صالحة في أحد البنود." }
        val amount = lines.sumOf { it.quantity * it.unitPrice }
        require(amount.isFinite() && amount >= 0.0) { "إجمالي المستند خارج النطاق المسموح." }
        return amount
    }
}
