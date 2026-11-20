package com.senac.viaja.ui.profile

import com.senac.viaja.data.model.StripeSubscription
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal enum class SubscriptionAction {
    CHECKOUT, RESUME_CHECKOUT, CANCEL, MANAGE, FIX_PAYMENT
}

internal enum class SubscriptionState {
    FREE, CHECKOUT_STARTED, CONFIRMING, PAYMENT_ISSUE, ACTIVE, ACTIVE_WITH_PAYMENT_ISSUE, ENDING, ENDED, UNKNOWN;

    fun action(canManage: Boolean): SubscriptionAction? = when (this) {
        FREE, ENDED -> SubscriptionAction.CHECKOUT
        CHECKOUT_STARTED -> SubscriptionAction.RESUME_CHECKOUT
        ACTIVE, ACTIVE_WITH_PAYMENT_ISSUE -> if (canManage) SubscriptionAction.CANCEL else null
        ENDING -> if (canManage) SubscriptionAction.MANAGE else null
        PAYMENT_ISSUE -> if (canManage) SubscriptionAction.FIX_PAYMENT else null
        CONFIRMING, UNKNOWN -> null
    }
}

internal fun StripeSubscription.displayState(checkoutOpened: Boolean = false): SubscriptionState = when {
    premiumActive && cancelAtPeriodEnd -> SubscriptionState.ENDING
    premiumActive && status == "past_due" -> SubscriptionState.ACTIVE_WITH_PAYMENT_ISSUE
    premiumActive -> SubscriptionState.ACTIVE
    checkoutOpened && status in setOf("free", "canceled", "incomplete_expired") -> SubscriptionState.CHECKOUT_STARTED
    status == "free" -> SubscriptionState.FREE
    status == "pending" -> SubscriptionState.CHECKOUT_STARTED
    status in setOf("canceled", "incomplete_expired") -> SubscriptionState.ENDED
    status in setOf("active", "trialing") -> SubscriptionState.CONFIRMING
    status in setOf("incomplete", "past_due", "unpaid", "paused") -> SubscriptionState.PAYMENT_ISSUE
    else -> SubscriptionState.UNKNOWN
}

internal fun formatPaidUntil(value: String): String? {
    val instant = Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?Z""")
    if (!instant.matches(value)) return null
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }
    val date = runCatching { parser.parse(value.substringBefore('.').removeSuffix("Z") + "Z") }.getOrNull()
        ?: return null
    return SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).format(date)
}
