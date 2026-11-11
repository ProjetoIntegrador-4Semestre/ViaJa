package com.senac.viaja.ui.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.senac.viaja.R
import com.senac.viaja.data.model.StripeHostedUrl
import com.senac.viaja.data.model.StripeSubscription
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.repository.BillingRepository
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent

class PremiumActivity : AppCompatActivity() {
    private val green = Color.rgb(12, 161, 95)
    private val ink = Color.rgb(25, 53, 41)
    private val muted = Color.rgb(111, 124, 115)
    private val cream = Color.rgb(248, 247, 241)
    private val line = Color.rgb(227, 230, 222)

    private enum class Screen(val title: String) {
        PLANS("Viajá Premium"), SUBSCRIPTION("Assinatura"), CHANGE("Alterar plano"),
        CONFIRM("Confirmar alteração"), PAYMENT("Formas de pagamento"), SUCCESS("Tudo certo")
    }

    private val billing = BillingRepository()
    private var screen = Screen.PLANS
    private var changeParent = Screen.PLANS
    private var subscription: StripeSubscription? = null
    private var loading = false
    private var awaitingCheckout = false
    private var message: String? = null

    private val canCheckout: Boolean
        get() = subscription?.status in setOf("free", "pending", "canceled", "incomplete_expired")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = savedInstanceState?.getString("screen")?.let(Screen::valueOf)
            ?: if (isBillingReturn(intent) || intent.getBooleanExtra(OPEN_SUBSCRIPTION, false)) {
                Screen.SUBSCRIPTION
            } else {
                Screen.PLANS
            }
        changeParent = savedInstanceState?.getString("changeParent")?.let(Screen::valueOf) ?: Screen.PLANS
        awaitingCheckout = savedInstanceState?.getBoolean("awaitingCheckout") ?: false
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goBack()
        })
        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("screen", screen.name)
        outState.putString("changeParent", changeParent.name)
        outState.putBoolean("awaitingCheckout", awaitingCheckout)
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (isBillingReturn(intent)) navigate(Screen.SUBSCRIPTION)
    }

    override fun onResume() {
        super.onResume()
        if (AuthRepository(this).getUser() == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            loadSubscription()
        }
    }

    private fun isBillingReturn(intent: Intent): Boolean {
        val uri = intent.data
        return intent.action == Intent.ACTION_VIEW && uri?.scheme == "viaja" &&
            uri.host == "billing" && uri.path == "/return"
    }

    private fun loadSubscription() {
        if (loading) return
        loading = true
        message = "Consultando assinatura…"
        render()
        billing.subscription({ state ->
            if (!isFinishing && !isDestroyed) {
                subscription = state
                loading = false
                message = null
                if (awaitingCheckout && state.premiumActive) {
                    awaitingCheckout = false
                    screen = Screen.SUCCESS
                } else if (!state.premiumActive && screen == Screen.SUCCESS) {
                    screen = Screen.SUBSCRIPTION
                }
                render()
            }
        }, ::showError)
    }

    private fun openHosted(checkout: Boolean) {
        val allowed = if (checkout) canCheckout else subscription?.canManage == true
        if (loading || !allowed) return
        val originatingScreen = screen
        val host = if (checkout) "buy.stripe.com" else "billing.stripe.com"
        loading = true
        message = "Abrindo Stripe…"
        render()
        val onSuccess: (StripeHostedUrl) -> Unit = { result ->
            if (!isFinishing && !isDestroyed) {
                val uri = Uri.parse(result.url)
                if (screen != originatingScreen) {
                    loading = false
                    message = null
                    render()
                } else if (uri.scheme != "https" || uri.host != host) {
                    showError("Endereço do Stripe inválido.")
                } else {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                        if (checkout) {
                            awaitingCheckout = true
                            screen = Screen.SUBSCRIPTION
                        }
                        loading = false
                        message = "Volte ao Viajá para atualizar sua assinatura."
                        render()
                    } catch (_: ActivityNotFoundException) {
                        showError("Nenhum navegador disponível para abrir o Stripe.")
                    }
                }
            }
        }
        if (checkout) billing.checkout(onSuccess, ::showError) else billing.portal(onSuccess, ::showError)
    }

    private fun showError(error: String) {
        if (isFinishing || isDestroyed) return
        loading = false
        message = error
        render()
    }

    private fun navigate(destination: Screen) {
        screen = destination
        render()
    }

    private fun choosePlan() {
        changeParent = screen
        navigate(Screen.CHANGE)
    }

    private fun goBack() {
        when (screen) {
            Screen.PLANS -> finish()
            Screen.SUBSCRIPTION -> navigate(Screen.PLANS)
            Screen.CHANGE -> navigate(changeParent)
            Screen.CONFIRM -> navigate(Screen.CHANGE)
            Screen.PAYMENT, Screen.SUCCESS -> navigate(Screen.SUBSCRIPTION)
        }
    }

    private fun subscriptionStatus(): String = when {
        subscription == null -> "Status da assinatura indisponível"
        subscription?.premiumActive == true && subscription?.cancelAtPeriodEnd == true -> "Premium ativo · Renovação cancelada"
        subscription?.premiumActive == true && subscription?.status == "past_due" -> "Premium ativo · Renovação pendente"
        subscription?.premiumActive == true -> "Premium ativo"
        subscription?.status in setOf("pending", "incomplete", "active") -> "Aguardando confirmação do Stripe"
        subscription?.status in setOf("past_due", "unpaid", "paused") -> "Assinatura com pendência"
        subscription?.status == "canceled" -> "Plano gratuito · Assinatura cancelada"
        else -> "Plano gratuito"
    }

    private fun subscriptionScreen(): View {
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(cream) }
        page.addView(View(this).apply { setBackgroundColor(green) }, LinearLayout.LayoutParams(-1, dp(2)))
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(20), dp(18), dp(20)) }
        body.addView(label("Viajá Premium", 21, ink, true), centered())
        body.addView(label("Consulte o status do seu plano e gerencie a assinatura.", 12, muted).apply { gravity = Gravity.CENTER }, centered().apply { topMargin = dp(8); bottomMargin = dp(18) })
        val status = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)); background = shape(Color.WHITE, 14) }
        status.addView(label(subscriptionStatus(), 12, green, true), LinearLayout.LayoutParams(0, -2, 1f))
        body.addView(status)
        subscription?.takeIf { it.premiumActive }?.paidUntil?.take(10)?.split("-")?.reversed()?.joinToString("/")?.let { date ->
            body.addView(infoRow("Período pago até", date), spaced(14, 8))
        }
        body.addView(infoRow("Ciclo do Premium", "Mensal"), spaced(8, 20))
        body.addView(label("Forma de Pagamento", 16, ink, true), spaced(0, 12))
        val payment = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = shape(Color.WHITE, 12) }
        payment.addView(label("▣", 22, muted), LinearLayout.LayoutParams(dp(38), -2))
        payment.addView(label("Gerenciada pelo Stripe", 11, ink), LinearLayout.LayoutParams(0, -2, 1f))
        payment.addView(button("Ver", Color.WHITE, green, true) { navigate(Screen.PAYMENT) })
        body.addView(payment)
        body.addView(button("✧  Alterar plano", green, Color.WHITE) { choosePlan() }, spaced(18, 10))
        if (subscription?.canManage == true && subscription?.status != "canceled" &&
            subscription?.cancelAtPeriodEnd != true) {
            body.addView(button("Cancelar assinatura", Color.WHITE, Color.rgb(160, 44, 41), true) {
                openHosted(checkout = false)
            }, spaced(0, 12))
        }
        body.addView(button("Atualizar status", Color.WHITE, green, true, ::loadSubscription), spaced(0, 8))
        scroll.addView(body)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return page
    }

    private fun plansScreen(): View {
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(cream) }
        val subscriptionLink = label("Minha assinatura", 10, muted, true).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(dp(16), 0, dp(16), 0)
            minimumHeight = dp(48)
            setBackgroundColor(Color.WHITE)
            setOnClickListener { navigate(Screen.SUBSCRIPTION) }
        }
        page.addView(subscriptionLink)
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(12), dp(16), dp(18)) }
        body.addView(label("✦  EXPERIÊNCIA PREMIUM", 10, Color.rgb(153, 125, 10), true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, dp(8)) }, centered())
        body.addView(label("Eleve Sua Jornada", 20, ink, true).apply { gravity = Gravity.CENTER }, centered())
        body.addView(label("Compare os planos e consulte sua assinatura.", 11, muted).apply { gravity = Gravity.CENTER }, centered().apply { topMargin = dp(8) })
        val toggle = label("Cobrança mensal", 10, ink, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(12), 0, dp(14)) }
        body.addView(toggle, centered())
        body.addView(planCard("Aventureiro", "Grátis", "/sempre", "Explore destinos\nSalve lugares\nMonte seu roteiro", if (subscription?.premiumActive == true) "Plano gratuito" else "Aventureiro", false), spaced(6, 14))
        body.addView(planCard("Viajá\nPremium", "Mensal", "no Stripe", "Preço e condições no Stripe", if (subscription?.premiumActive == true) "Minha assinatura" else "Alterar agora", true), spaced(0, 14))
        body.addView(button("Gerenciar assinatura", Color.WHITE, green, true) { navigate(Screen.SUBSCRIPTION) }, spaced(0, 8))
        scroll.addView(body)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return page
    }

    private fun planCard(title: String, price: String, suffix: String, features: String, cta: String, premium: Boolean): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(16))
            background = shape(Color.WHITE, 16, if (premium) green else line)
        }
        if (premium) card.addView(label("VIAJÁ PREMIUM                         RECOMENDADO", 9, green, true), spaced(0, 12))
        card.addView(label(title, 17, if (premium) green else ink, true))
        if (!premium) card.addView(label("Recursos essenciais para viajar.", 10, muted), spaced(2, 12))
        val priceRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        priceRow.addView(label(price, 21, ink, true))
        priceRow.addView(label("  $suffix", 9, muted))
        card.addView(priceRow, spaced(0, 12))
        features.split("\n").forEach { feature -> card.addView(label("✓  $feature", 10, if (premium) ink else muted), spaced(0, 9)) }
        if (premium) {
            card.addView(button(cta, green, Color.WHITE) {
                if (subscription?.premiumActive == true) navigate(Screen.SUBSCRIPTION) else choosePlan()
            }, spaced(16, 6))
        } else {
            card.addView(button(cta, Color.WHITE, ink, true), spaced(16, 6))
        }
        return card
    }

    private fun infoRow(left: String, right: String) = LinearLayout(this).apply {
        gravity = Gravity.CENTER_VERTICAL
        addView(label(left, 10, muted), LinearLayout.LayoutParams(0, -2, 1f))
        addView(label(right, 11, ink, true).apply { gravity = Gravity.END })
    }

    private fun button(text: String, color: Int, textColor: Int, outline: Boolean = false, onClick: (() -> Unit)? = null) = label(text, 11, textColor, true).apply {
        gravity = Gravity.CENTER
        setPadding(dp(10), dp(11), dp(10), dp(11))
        background = shape(color, 28, if (outline) line else color)
        if (onClick != null) { isClickable = true; setOnClickListener { onClick() } }
    }

    private fun render() {
        val content = when (screen) {
            Screen.PLANS -> plansScreen()
            Screen.SUBSCRIPTION -> subscriptionScreen()
            Screen.CHANGE -> changePlanScreen()
            Screen.CONFIRM -> confirmationScreen()
            Screen.PAYMENT -> paymentMethodsScreen()
            Screen.SUCCESS -> successScreen("Assinatura confirmada", "O Stripe confirmou seu acesso ao Viajá Premium.")
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cream)
            message?.let { status ->
                addView(label(status, 12, ink, true).apply {
                    gravity = Gravity.CENTER
                    accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
                    setPadding(dp(16), dp(6), dp(16), dp(6))
                }, LinearLayout.LayoutParams(-1, -2))
            }
            addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        }
        setPageContent(root, screen.title, PageTab.PROFILE, onBack = ::goBack)
    }

    private fun page(): Pair<LinearLayout, LinearLayout> {
        val screen = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(cream) }
        screen.addView(View(this).apply { setBackgroundColor(green) }, LinearLayout.LayoutParams(-1, dp(2)))
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(24), dp(20), dp(24)) }
        scroll.addView(body)
        screen.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return screen to body
    }

    private fun summaryCard(title: String, price: String, detail: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = shape(Color.WHITE, 14, line)
        addView(label(title, 14, ink, true))
        addView(label(price, 17, green, true), spaced(8, 4))
        addView(label(detail, 10, muted))
    }

    private fun confirmationScreen(): View {
        val (screen, body) = page()
        body.addView(label("Confirme seu novo plano", 21, ink, true), centered())
        body.addView(label("Revise os detalhes antes de atualizar sua assinatura.", 12, muted).apply { gravity = Gravity.CENTER }, spaced(8, 24))
        body.addView(label("PLANO ATUAL", 10, muted, true), spaced(0, 8))
        body.addView(summaryCard("Sua assinatura", subscriptionStatus(), "Consulte seu plano na página de assinatura."), spaced(0, 18))
        body.addView(label("NOVO PLANO", 10, green, true), spaced(0, 8))
        body.addView(summaryCard("Viajá Premium Mensal", "Cobrança mensal", "Veja o valor no Stripe."), spaced(0, 18))
        body.addView(label("Use no Stripe o mesmo e-mail da sua conta Viajá.", 11, muted), spaced(0, 24))
        if (canCheckout) {
            body.addView(button("Continuar no Stripe", green, Color.WHITE) { openHosted(checkout = true) }, spaced(12, 10))
        } else if (subscription?.canManage == true) {
            body.addView(button("Gerenciar no Stripe", green, Color.WHITE) { openHosted(checkout = false) }, spaced(12, 10))
        } else {
            body.addView(button("Atualizar status", Color.WHITE, green, true, ::loadSubscription), spaced(12, 10))
        }
        body.addView(button("Voltar", Color.WHITE, green, true, ::goBack), spaced(0, 8))
        return screen
    }

    private fun changePlanScreen(): View {
        val (screen, body) = page()
        body.addView(label("Escolha seu plano", 22, ink, true), centered())
        body.addView(label("Selecione a opção que combina com sua próxima jornada.", 12, muted).apply { gravity = Gravity.CENTER }, spaced(8, 20))
        body.addView(summaryCard("Aventureiro", "Grátis", "Recursos básicos para planejar viagens"), spaced(0, 12))
        body.addView(summaryCard("Viajá Premium Mensal", "Cobrança mensal", "Preço e condições no Stripe"), spaced(0, 20))
        body.addView(button("Revisar plano", green, Color.WHITE) { navigate(Screen.CONFIRM) }, spaced(8, 10))
        body.addView(button("Voltar", Color.WHITE, green, true, ::goBack))
        return screen
    }

    private fun paymentMethodsScreen(): View {
        val (screen, body) = page()
        body.addView(label("Seus pagamentos", 22, ink, true), centered())
        body.addView(label("Gerencie o cartão usado na assinatura Viajá Premium.", 12, muted), spaced(8, 22))
        body.addView(label("FORMA ATUAL", 10, muted, true), spaced(0, 8))
        val hasPortal = subscription?.canManage == true
        body.addView(summaryCard("Forma de pagamento", if (hasPortal) "Consulte no Stripe" else "Sem assinatura vinculada",
            if (hasPortal) "Consulte o método atual e adicione outro no portal." else "O método de pagamento é escolhido ao assinar."), spaced(0, 12))
        body.addView(label("GERENCIAMENTO", 10, muted, true), spaced(0, 12))
        body.addView(summaryCard("Portal do Stripe", "Pagamento seguro", "Gerencie os dados de cobrança da assinatura."), spaced(0, 20))
        if (hasPortal) {
            body.addView(button("Abrir portal do Stripe", green, Color.WHITE) { openHosted(checkout = false) }, spaced(8, 10))
        } else if (canCheckout) {
            body.addView(button("Escolher plano", green, Color.WHITE) { choosePlan() }, spaced(8, 10))
        }
        body.addView(button("Voltar à assinatura", Color.WHITE, green, true, ::goBack))
        return screen
    }

    private fun successScreen(title: String, message: String): View {
        val (screen, body) = page()
        body.gravity = Gravity.CENTER
        body.addView(label("✓", 42, green, true).apply { gravity = Gravity.CENTER }, centered())
        body.addView(label(title, 22, ink, true).apply { gravity = Gravity.CENTER }, spaced(12, 8))
        body.addView(label(message, 13, muted).apply { gravity = Gravity.CENTER }, spaced(0, 24))
        body.addView(button("Voltar à assinatura", green, Color.WHITE) { navigate(Screen.SUBSCRIPTION) })
        return screen
    }
    private fun label(text: String, size: Int, color: Int, bold: Boolean = false) = TextView(this).apply {
        this.text = text
        textSize = size.toFloat()
        setTextColor(color)
        typeface = ResourcesCompat.getFont(this@PremiumActivity, if (bold) R.font.be_vietnam_pro_extrabold else R.font.plus_jakarta_sans_regular) ?: Typeface.DEFAULT
        includeFontPadding = true
    }

    private fun shape(color: Int, radius: Int, stroke: Int = color) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        setStroke(dp(1), stroke)
    }

    private fun centered() = LinearLayout.LayoutParams(-1, -2)
    private fun spaced(top: Int, bottom: Int) = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top); bottomMargin = dp(bottom) }
    companion object {
        internal const val OPEN_SUBSCRIPTION = "open_subscription"
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
