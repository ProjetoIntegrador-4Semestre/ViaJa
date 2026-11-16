package com.senac.viaja.ui.trips

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

internal data class ExpenseFormValues(val description: String, val amountCents: Long, val date: String, val category: String, val reimbursement: String)

internal object ExpenseRules {
    val categories = linkedMapOf("transport" to "Transporte", "lodging" to "Hospedagem", "food" to "Alimentação",
        "activities" to "Passeios", "shopping" to "Compras", "other" to "Outros")
    val reimbursements = linkedMapOf("none" to "Não reembolsável", "pending" to "A solicitar", "requested" to "Solicitado", "received" to "Recebido")

    fun currency(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(BigDecimal.valueOf(cents, 2))

    fun remaining(budget: Long?, total: Long): Long? = budget?.let { Math.subtractExact(it, total) }

    fun balanceLabel(remaining: Long?): String = when {
        remaining == null -> "Defina um orçamento para acompanhar o saldo."
        remaining < 0 -> "Acima do orçamento: ${currency(Math.negateExact(remaining))}"
        else -> "Saldo do orçamento: ${currency(remaining)}"
    }

    fun validate(description: String, amount: String, date: String, category: String, reimbursement: String): ExpenseFormValues {
        val label = description.trim()
        require(label.isNotEmpty() && label.length <= 160) { "Descreva a despesa em até 160 caracteres." }
        require(amount.isNotBlank()) { "Informe o valor da despesa." }
        require(date.isNotBlank()) { "Informe a data da despesa." }
        val values = TripFormRules.validate("Despesa", date, "", amount)
        val cents = requireNotNull(values.budgetCents)
        require(cents > 0) { "O valor da despesa precisa ser maior que zero." }
        require(category in categories) { "Escolha uma categoria válida." }
        require(reimbursement in reimbursements) { "Escolha um estado de reembolso válido." }
        return ExpenseFormValues(label, cents, requireNotNull(values.startDate), category, reimbursement)
    }
}
