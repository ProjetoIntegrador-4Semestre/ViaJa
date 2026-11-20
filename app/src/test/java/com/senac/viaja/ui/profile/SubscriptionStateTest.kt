package com.senac.viaja.ui.profile

import com.senac.viaja.data.model.StripeSubscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubscriptionStateTest {
    @Test
    fun freeOrEndedAccountCanSubscribeEvenIfItStillHasAStripeCustomer() {
        assertEquals(SubscriptionAction.CHECKOUT, subscription("free").displayState().action(false))
        for (status in listOf("canceled", "incomplete_expired")) {
            val ended = subscription(status, canManage = true, cancelAtPeriodEnd = true)
            assertEquals(SubscriptionState.ENDED, ended.displayState())
            assertEquals(SubscriptionAction.CHECKOUT, ended.displayState().action(ended.canManage))
        }
    }

    @Test
    fun anOpenedCheckoutIsNotTreatedAsPaymentConfirmation() {
        assertEquals(SubscriptionState.CHECKOUT_STARTED, subscription("pending").displayState())
        val returning = subscription("canceled").displayState(checkoutOpened = true)
        assertEquals(SubscriptionState.CHECKOUT_STARTED, returning)
        assertEquals(SubscriptionAction.RESUME_CHECKOUT, returning.action(false))
        assertEquals(SubscriptionState.CONFIRMING, subscription("active").displayState(checkoutOpened = true))
    }

    @Test
    fun activeAccessAllowsCancelAndScheduledCancellationStillKeepsAccess() {
        val active = subscription("active", premiumActive = true, canManage = true)
        assertEquals(SubscriptionAction.CANCEL, active.displayState().action(active.canManage))
        assertNull(active.displayState().action(false))
        val ending = active.copy(cancelAtPeriodEnd = true)
        assertEquals(SubscriptionState.ENDING, ending.displayState())
        assertEquals(SubscriptionAction.MANAGE, ending.displayState().action(true))
        assertNull(ending.displayState().action(false))
    }

    @Test
    fun overdueRenewalKeepsAccessOnlyWhenTheServerConfirmsIt() {
        val overdue = subscription("past_due", canManage = true)
        assertEquals(SubscriptionAction.FIX_PAYMENT, overdue.displayState().action(true))
        val stillPaid = overdue.copy(premiumActive = true)
        assertEquals(SubscriptionState.ACTIVE_WITH_PAYMENT_ISSUE, stillPaid.displayState())
        assertEquals(SubscriptionAction.CANCEL, stillPaid.displayState().action(true))
    }

    @Test
    fun unresolvedSubscriptionsDoNotOfferAnotherCheckout() {
        for (status in listOf("incomplete", "unpaid", "paused")) {
            val state = subscription(status).displayState()
            assertEquals(SubscriptionState.PAYMENT_ISSUE, state)
            assertNull(state.action(false))
        }
        for (status in listOf("active", "trialing", "unknown")) {
            assertNull(subscription(status).displayState().action(true))
        }
    }

    private fun subscription(
        status: String,
        premiumActive: Boolean = false,
        canManage: Boolean = false,
        cancelAtPeriodEnd: Boolean = false
    ) = StripeSubscription(status, premiumActive, null, cancelAtPeriodEnd, canManage)
}
