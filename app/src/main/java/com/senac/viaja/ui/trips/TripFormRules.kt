package com.senac.viaja.ui.trips

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal data class TripFormValues(
    val name: String,
    val startDate: String?,
    val endDate: String?,
    val budgetCents: Long?
)

internal object TripFormRules {
    private fun format(pattern: String) = SimpleDateFormat(pattern, Locale.US).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun displayDate(iso: String?): String = iso?.let {
        runCatching { format("dd/MM/yyyy").format(format("yyyy-MM-dd").parse(it)!!) }.getOrNull()
    }.orEmpty()

    fun datesLabel(start: String?, end: String?): String = when {
        start == null -> "Datas a definir"
        end == null -> "A partir de ${displayDate(start)}"
        start == end -> displayDate(start)
        else -> "${displayDate(start)} — ${displayDate(end)}"
    }

    fun budgetInput(cents: Long?): String = cents?.let {
        BigDecimal.valueOf(it, 2).toPlainString().replace('.', ',')
    }.orEmpty()

    fun validate(name: String, start: String, end: String, budget: String, lastStopDay: Int = 0): TripFormValues {
        require(name.isNotBlank()) { "Dê um nome à viagem." }
        fun date(value: String): String? {
            if (value.isBlank()) return null
            require(Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}").matches(value)) { "Use datas no formato dd/mm/aaaa." }
            val parsed = runCatching { format("dd/MM/yyyy").parse(value) }.getOrNull()
            require(parsed != null) { "Informe uma data válida." }
            return format("yyyy-MM-dd").format(parsed)
        }
        val first = date(start.trim())
        val last = date(end.trim())
        require(last == null || (first != null && last >= first)) { "A data final precisa ser igual ou posterior à inicial." }
        if (first != null && last != null) {
            val days = (format("yyyy-MM-dd").parse(last)!!.time - format("yyyy-MM-dd").parse(first)!!.time) / 86_400_000L + 1
            require(days >= lastStopDay) { "Seu roteiro tem paradas até o dia $lastStopDay. Ajuste o roteiro antes de reduzir a duração." }
        }
        val raw = budget.trim()
        val cents = if (raw.isBlank()) null else {
            require(Regex("(?:[0-9]+|[0-9]{1,3}(?:\\.[0-9]{3})+)(?:,[0-9]{1,2})?").matches(raw)) {
                "Informe o orçamento em reais, como 2500,00."
            }
            val value = BigDecimal(raw.replace(".", "").replace(',', '.')).movePointRight(2)
            require(value <= BigDecimal(Int.MAX_VALUE)) { "O orçamento máximo é R$ 21.474.836,47." }
            value.longValueExact()
        }
        return TripFormValues(name.trim(), first, last, cents)
    }
}
