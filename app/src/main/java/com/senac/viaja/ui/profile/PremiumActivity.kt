package com.senac.viaja.ui.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.senac.viaja.R
import com.senac.viaja.data.model.StripeSubscription
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.repository.BillingRepository
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.ProfileActivity
import com.senac.viaja.ui.main.setPageContent
import retrofit2.Call
import java.net.URI

class PremiumActivity : AppCompatActivity() {
    private val auth by lazy { AuthRepository(this) }
    private val billing = BillingRepository()
    private var request: Call<*>? = null
    private var subscription: StripeSubscription? = null
    private var signedIn = false
    private var notice: Int? = null
    private var checkoutOwner: Long? = null
    private var openingBrowser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Viaja_Journey)
        super.onCreate(savedInstanceState)
        checkoutOwner = savedInstanceState?.getLong("checkoutOwner")?.takeIf { it > 0 }
        setPageContent(R.layout.activity_subscription, getString(R.string.subscription_title), PageTab.PROFILE) {
            if (isTaskRoot) startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
        findViewById<MaterialButton>(R.id.subscriptionRefresh).setOnClickListener { refresh() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        checkoutOwner?.let { outState.putLong("checkoutOwner", it) }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onPause() {
        request?.cancel()
        request = null
        openingBrowser = false
        super.onPause()
    }

    private fun refresh(@StringRes message: Int? = null) {
        if (request != null || openingBrowser) return
        subscription = null
        notice = message
        val owner = auth.getUser()?.id
        signedIn = owner != null
        if (checkoutOwner != owner) checkoutOwner = null
        if (owner != null) {
            request = billing.subscription({ current ->
                request = null
                if (auth.getUser()?.id != owner) {
                    refresh()
                } else {
                    subscription = current
                    if (current.displayState(checkoutOpened = true) != SubscriptionState.CHECKOUT_STARTED) {
                        checkoutOwner = null
                    }
                    render()
                }
            }, { code ->
                request = null
                if (auth.getUser()?.id != owner) refresh() else showError(code)
            })
        }
        render()
    }

    private fun openStripe(action: SubscriptionAction) {
        if (request != null || openingBrowser) return
        val owner = auth.getUser()?.id
        if (owner == null) {
            refresh()
            return
        }
        val checkout = action == SubscriptionAction.CHECKOUT || action == SubscriptionAction.RESUME_CHECKOUT
        val host = if (checkout) "buy.stripe.com" else "billing.stripe.com"
        notice = R.string.subscription_opening_stripe
        request = billing.hosted(checkout, { result ->
            request = null
            if (auth.getUser()?.id != owner) {
                refresh()
            } else {
                openHostedLink(result.url, host, owner.takeIf { checkout })
            }
        }, { code ->
            request = null
            when {
                auth.getUser()?.id != owner -> refresh()
                code == 409 -> refresh(R.string.subscription_conflict)
                code == 404 && !checkout -> refresh(R.string.subscription_service_unavailable)
                else -> showError(code)
            }
        })
        render()
    }

    private fun openHostedLink(url: String, host: String, buyer: Long?) {
        val uri = runCatching { URI(url) }.getOrNull()
        notice = if (uri?.scheme != "https" || uri.host != host || uri.rawUserInfo != null || uri.port != -1) {
            R.string.subscription_invalid_link
        } else {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString())))
                openingBrowser = true
                if (buyer != null) checkoutOwner = buyer
                R.string.subscription_opening_stripe
            } catch (_: ActivityNotFoundException) {
                R.string.subscription_no_browser
            }
        }
        render()
    }

    private fun showError(code: Int?) {
        notice = when (code) {
            502, 503 -> R.string.subscription_service_unavailable
            null -> R.string.subscription_connection_failed
            else -> R.string.subscription_request_failed
        }
        render()
    }

    private fun render() {
        val current = subscription
        val state = current?.displayState(checkoutOpened = checkoutOwner != null)
        val busy = request != null || openingBrowser
        val canManage = current?.canManage == true
        val copy = when {
            !signedIn -> R.string.subscription_guest to R.string.subscription_guest_detail
            state != null -> state.copy(canManage)
            busy -> R.string.subscription_loading to null
            else -> R.string.subscription_unavailable to R.string.subscription_unavailable_detail
        }
        findViewById<TextView>(R.id.subscriptionStatus).setText(copy.first)
        findViewById<TextView>(R.id.subscriptionDetail).apply {
            isVisible = copy.second != null
            copy.second?.let { setText(it) }
        }
        findViewById<TextView>(R.id.subscriptionPaidUntil).apply {
            val date = current?.takeIf { it.premiumActive }?.paidUntil?.let(::formatPaidUntil)
            isVisible = date != null
            date?.let { text = getString(R.string.subscription_paid_until, it) }
        }
        findViewById<TextView>(R.id.subscriptionBenefits).isVisible =
            state == SubscriptionState.FREE || state == SubscriptionState.ENDED
        val action = state?.action(canManage)
        findViewById<MaterialButton>(R.id.subscriptionPrimary).apply {
            isVisible = !signedIn || action != null
            isEnabled = !busy
            when {
                !signedIn -> {
                    setText(R.string.profile_login)
                    setOnClickListener {
                        startActivity(Intent(this@PremiumActivity, LoginActivity::class.java)
                            .putExtra(LoginActivity.RETURN_TO_SUBSCRIPTION, true))
                    }
                }
                action != null -> {
                    setText(action.label())
                    setOnClickListener { openStripe(action) }
                }
                else -> setOnClickListener(null)
            }
        }
        findViewById<MaterialButton>(R.id.subscriptionRefresh).apply {
            isVisible = signedIn && (!busy || current != null)
            isEnabled = !busy
        }
        findViewById<TextView>(R.id.subscriptionFeedback).apply {
            val message = notice
            isVisible = message != null && signedIn
            message?.let { setText(it) }
        }
    }

    private fun SubscriptionState.copy(canManage: Boolean): Pair<Int, Int> = when (this) {
        SubscriptionState.FREE -> R.string.subscription_free to R.string.subscription_free_detail
        SubscriptionState.CHECKOUT_STARTED -> R.string.subscription_checkout_started to R.string.subscription_checkout_started_detail
        SubscriptionState.CONFIRMING -> R.string.subscription_confirming to R.string.subscription_confirming_detail
        SubscriptionState.PAYMENT_ISSUE -> R.string.subscription_payment_issue to
            if (canManage) R.string.subscription_payment_issue_detail else R.string.subscription_payment_issue_no_portal
        SubscriptionState.ACTIVE -> R.string.subscription_active to
            if (canManage) R.string.subscription_active_detail else R.string.subscription_active_no_portal
        SubscriptionState.ACTIVE_WITH_PAYMENT_ISSUE -> R.string.subscription_renewal_issue to
            if (canManage) R.string.subscription_renewal_issue_detail else R.string.subscription_active_no_portal
        SubscriptionState.ENDING -> R.string.subscription_ending to R.string.subscription_ending_detail
        SubscriptionState.ENDED -> R.string.subscription_free to R.string.subscription_ended_detail
        SubscriptionState.UNKNOWN -> R.string.subscription_unavailable to R.string.subscription_unavailable_detail
    }

    @StringRes
    private fun SubscriptionAction.label(): Int = when (this) {
        SubscriptionAction.CHECKOUT -> R.string.subscription_checkout
        SubscriptionAction.RESUME_CHECKOUT -> R.string.subscription_resume_checkout
        SubscriptionAction.CANCEL -> R.string.subscription_cancel
        SubscriptionAction.MANAGE -> R.string.subscription_manage
        SubscriptionAction.FIX_PAYMENT -> R.string.subscription_fix_payment
    }
}
