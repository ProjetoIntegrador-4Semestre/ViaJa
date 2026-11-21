package com.senac.viaja.ui.main

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senac.viaja.R
import com.senac.viaja.data.model.AccountProfile
import com.senac.viaja.data.model.StripeSubscription
import com.senac.viaja.data.model.ViajaUser
import com.senac.viaja.data.remote.ApiClient
import com.senac.viaja.data.remote.deliver
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.repository.BillingRepository
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.legal.TermsPrivacyActivity
import com.senac.viaja.ui.profile.EditProfileActivity
import com.senac.viaja.ui.profile.PremiumActivity
import com.senac.viaja.ui.profile.SubscriptionState
import com.senac.viaja.ui.profile.displayState
import com.senac.viaja.ui.profile.formatPaidUntil
import com.senac.viaja.ui.trips.OfflineTripsActivity
import retrofit2.Call

class ProfileActivity : AppCompatActivity() {
    private val auth by lazy { AuthRepository(this) }
    private val billing = BillingRepository()
    private var accountRequest: Call<AccountProfile>? = null
    private var subscriptionRequest: Call<StripeSubscription>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_profile, getString(R.string.profile_title), PageTab.PROFILE, topLevel = true) {
            if (isTaskRoot) startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener { editProfile() }
        findViewById<MaterialButton>(R.id.btnProfileTourism).setOnClickListener { editProfile(mode = "tourism") }
        findViewById<MaterialButton>(R.id.btnProfileWork).setOnClickListener { editProfile(mode = "work") }
        findViewById<MaterialButton>(R.id.btnEditInterests).setOnClickListener { editProfile(interests = true) }
        findViewById<MaterialButton>(R.id.btnManageSubscription).setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnProfileOffline).setOnClickListener {
            startActivity(Intent(this, OfflineTripsActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnProfileTerms).setOnClickListener {
            startActivity(Intent(this, TermsPrivacyActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnProfileLogout).setOnClickListener { confirmLogout() }
    }

    override fun onResume() {
        super.onResume()
        val user = auth.getUser()
        showIdentity(user)
        loadSubscriptionSummary(user)
        showInterests(message = getString(
            if (user == null) R.string.profile_interests_guest else R.string.profile_interests_loading
        ))
        if (user == null) return

        val request = ApiClient.service.account()
        accountRequest = request
        request.deliver({ profile ->
            if (accountRequest !== request) return@deliver
            val currentUser = auth.getUser()
            if (currentUser?.id != user.id || profile.id != user.id) {
                showIdentity(currentUser)
                showInterests(message = getString(
                    if (currentUser == null) R.string.profile_interests_guest else R.string.profile_interests_error
                ))
                return@deliver
            }
            showIdentity(ViajaUser(profile.id, profile.email, profile.name, profile.defaultMode))
            val selectedIds = profile.interestIds.toSet()
            val names = profile.interests.filter { it.id in selectedIds }.map { it.name }
            showInterests(names, getString(R.string.profile_interests_empty))
        }, { error ->
            if (accountRequest === request) {
                if (auth.getUser() == null) showIdentity(null)
                showInterests(message = error)
            }
        })
    }

    override fun onPause() {
        accountRequest?.cancel()
        accountRequest = null
        subscriptionRequest?.cancel()
        subscriptionRequest = null
        super.onPause()
    }

    private fun loadSubscriptionSummary(user: ViajaUser?) {
        if (user == null) {
            showSubscriptionSummary(R.string.profile_guest, detail = getString(R.string.subscription_summary_guest))
            return
        }
        showSubscriptionSummary(R.string.subscription_loading)
        subscriptionRequest = billing.subscription({ subscription ->
            if (auth.getUser()?.id == user.id) {
                showSubscriptionSummary(subscription)
            } else {
                showSubscriptionUnavailable()
            }
        }, { showSubscriptionUnavailable() })
    }

    private fun showSubscriptionUnavailable() {
        if (auth.getUser() == null) {
            showSubscriptionSummary(R.string.profile_guest, detail = getString(R.string.subscription_summary_guest))
        } else {
            showSubscriptionSummary(R.string.subscription_unavailable, detail = getString(R.string.subscription_summary_unavailable))
        }
    }

    private fun showSubscriptionSummary(subscription: StripeSubscription) {
        val state = subscription.displayState()
        if (state == SubscriptionState.UNKNOWN) {
            showSubscriptionUnavailable()
            return
        }
        val detail = when (state) {
            SubscriptionState.ENDING -> subscription.paidUntil?.let(::formatPaidUntil)?.let {
                getString(R.string.subscription_summary_ending, it)
            } ?: getString(R.string.subscription_summary_ending_no_date)
            SubscriptionState.ACTIVE_WITH_PAYMENT_ISSUE -> getString(R.string.subscription_summary_renewal_issue)
            SubscriptionState.ACTIVE -> getString(R.string.subscription_summary_premium)
            SubscriptionState.CHECKOUT_STARTED -> getString(R.string.subscription_checkout_started)
            SubscriptionState.CONFIRMING -> getString(R.string.subscription_confirming)
            SubscriptionState.PAYMENT_ISSUE -> getString(R.string.subscription_payment_issue)
            else -> getString(R.string.subscription_summary_free)
        }
        showSubscriptionSummary(
            plan = if (subscription.premiumActive) R.string.subscription_plan_premium else R.string.subscription_free,
            price = if (subscription.premiumActive) R.string.subscription_price_premium else R.string.subscription_price_free,
            detail = detail
        )
    }

    private fun showSubscriptionSummary(plan: Int, price: Int? = null, detail: String? = null) {
        findViewById<TextView>(R.id.profileSubscriptionPlan).setText(plan)
        findViewById<TextView>(R.id.profileSubscriptionPrice).apply {
            isVisible = price != null
            price?.let { setText(it) }
        }
        findViewById<TextView>(R.id.profileSubscriptionDetail).apply {
            isVisible = detail != null
            text = detail
        }
    }

    private fun showIdentity(user: ViajaUser?) {
        val status = getString(if (user == null) R.string.profile_guest else R.string.profile_signed_in)
        findViewById<TextView>(R.id.tvProfileName).text =
            user?.name?.takeIf { it.isNotBlank() } ?: getString(R.string.profile_traveler)
        findViewById<TextView>(R.id.tvProfileRole).setText(
            if (user == null) R.string.profile_guest else R.string.profile_traveler
        )
        findViewById<TextView>(R.id.tvProfileEmail).text =
            getString(R.string.profile_email, user?.email ?: getString(R.string.profile_email_missing))
        findViewById<TextView>(R.id.tvProfileStatus).text = getString(R.string.profile_account_status, status)
        findViewById<MaterialButton>(R.id.btnEditProfile).setText(
            if (user == null) R.string.profile_login else R.string.profile_edit
        )
        findViewById<MaterialButton>(R.id.btnProfileLogout).isVisible = user != null

        val mode = user?.defaultMode
        val tourism = mode == "tourism"
        val work = mode == "work"
        findViewById<TextView>(R.id.tvProfileMode).apply {
            isVisible = tourism || work
            setText(if (work) R.string.profile_work else R.string.profile_tourism)
        }
        showMode(R.id.btnProfileTourism, tourism)
        showMode(R.id.btnProfileWork, work)
    }

    private fun showMode(buttonId: Int, selected: Boolean) {
        findViewById<MaterialButton>(buttonId).apply {
            isSelected = selected
            backgroundTintList = ColorStateList.valueOf(if (selected) Color.WHITE else Color.TRANSPARENT)
        }
    }

    private fun showInterests(names: List<String> = emptyList(), message: String) {
        val chips = findViewById<ChipGroup>(R.id.profileInterestChips)
        chips.removeAllViews()
        names.forEach { name ->
            val chip = layoutInflater.inflate(R.layout.item_profile_interest, chips, false) as TextView
            chip.text = name
            chips.addView(chip)
        }
        chips.isVisible = names.isNotEmpty()
        findViewById<TextView>(R.id.tvProfileInterestsStatus).apply {
            text = message
            isVisible = names.isEmpty()
        }
    }

    private fun editProfile(mode: String? = null, interests: Boolean = false) {
        val destination = if (auth.getUser() == null) {
            Intent(this, LoginActivity::class.java)
        } else {
            EditProfileActivity.open(this, mode, interests)
        }
        startActivity(destination)
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profile_logout_title)
            .setNegativeButton(R.string.profile_logout_keep, null)
            .setPositiveButton(R.string.profile_logout_confirm) { _, _ ->
                auth.clearSession()
                startActivity(Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                finish()
            }
            .show()
    }
}
