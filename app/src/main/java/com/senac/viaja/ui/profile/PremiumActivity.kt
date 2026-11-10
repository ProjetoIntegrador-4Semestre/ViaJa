package com.senac.viaja.ui.profile

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.senac.viaja.R
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent

class PremiumActivity : AppCompatActivity() {
    private val green = Color.rgb(12, 161, 95)
    private val ink = Color.rgb(25, 53, 41)
    private val muted = Color.rgb(111, 124, 115)
    private val cream = Color.rgb(248, 247, 241)
    private val line = Color.rgb(227, 230, 222)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showPage(plansScreen())
    }
    private fun phone(content: View): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = shape(Color.WHITE, 22)
        clipToOutline = true
        elevation = dp(8).toFloat()
        addView(content, LinearLayout.LayoutParams(-1, -1))
    }

    private fun subscriptionScreen(): View {
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(cream) }
        page.addView(View(this).apply { setBackgroundColor(green) }, LinearLayout.LayoutParams(-1, dp(2)))
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(20), dp(18), dp(20)) }
        body.addView(label("Viajá Premium", 21, ink, true), centered())
        body.addView(label("Sua assinatura está ativa e você possui acesso a todos os benefícios premium.", 12, muted).apply { gravity = Gravity.CENTER }, centered().apply { topMargin = dp(8); bottomMargin = dp(18) })
        val status = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)); background = shape(Color.WHITE, 14) }
        status.addView(label("●  ATIVO", 10, green, true), LinearLayout.LayoutParams(0, -2, 1f))
        status.addView(label("R$ 29,90", 21, ink, true).apply { gravity = Gravity.END })
        body.addView(status)
        body.addView(infoRow("Próxima cobrança", "15 de Nov, 2023"), spaced(14, 8))
        body.addView(infoRow("Ciclo de faturamento", "Mensal"), spaced(8, 20))
        body.addView(label("Forma de Pagamento", 16, ink, true), spaced(0, 12))
        val payment = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = shape(Color.WHITE, 12) }
        payment.addView(label("▣", 22, muted), LinearLayout.LayoutParams(dp(38), -2))
        payment.addView(label("Mastercard\nFinal 4242\nExpira em 12/25", 11, ink), LinearLayout.LayoutParams(0, -2, 1f))
        payment.addView(label("Editar", 11, green, true).apply { isClickable = true; setOnClickListener { showPage(paymentMethodsScreen(), "Formas de pagamento", ::showSubscription) } })
        body.addView(payment)
        body.addView(button("✧  Alterar plano", green, Color.WHITE) { showPage(confirmationScreen(), "Confirmar alteração", ::showSubscription) }, spaced(18, 10))
        body.addView(label("⊗  Cancelar assinatura", 11, Color.rgb(215, 62, 55), true).apply { gravity = Gravity.CENTER }, spaced(0, 18))
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
            setOnClickListener { showSubscription() }
        }
        page.addView(subscriptionLink)
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(12), dp(16), dp(18)) }
        body.addView(label("✦  EXPERIÊNCIA PREMIUM", 10, Color.rgb(153, 125, 10), true).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, dp(8)) }, centered())
        body.addView(label("Eleve Sua Jornada", 20, ink, true).apply { gravity = Gravity.CENTER }, centered())
        body.addView(label("Descubra recursos avançados para quem explora o mundo. De roteiros personalizados à organização prática, o Viajá Premium torna cada viagem inesquecível.", 11, muted).apply { gravity = Gravity.CENTER }, centered().apply { topMargin = dp(8) })
        val toggle = label("Mensal       Economia 20%       Anual", 10, ink, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(12), 0, dp(14)) }
        body.addView(toggle, centered())
        body.addView(planCard("Aventureiro", "$0", "/sempre", "Planejamento básico\nAcesso a guias públicos\nMapas básicos", "Plano atual", false), spaced(6, 14))
        body.addView(planCard("Viajá\nPremium", "R$9,99", "/mês, cobrança anual", "Personalização completa\nCriação avançada de roteiros\nOrganização aprimorada\nAcesso completo sem internet", "Alterar agora", true), spaced(0, 14))
        body.addView(button("Gerenciar assinatura", Color.WHITE, green, true) { showSubscription() }, spaced(0, 8))
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
        card.addView(button(cta, if (premium) green else Color.WHITE, if (premium) Color.WHITE else ink, !premium) { if (premium) showPage(changePlanScreen(), "Alterar plano", ::showSubscription) }, spaced(16, 6))
        if (premium) card.addView(label("Cancele quando quiser. Teste grátis por 7 dias.", 8, muted).apply { gravity = Gravity.CENTER }, centered().apply { topMargin = dp(8) })
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

    private fun showSubscription() {
        showPage(subscriptionScreen(), "Assinatura")
    }

    private fun showPage(content: View, title: String = "Viajá Premium", onBack: () -> Unit = ::finish) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cream)
            addView(label("Demonstração · sem cobrança", 12, ink, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(16), dp(6), dp(16), dp(6))
            }, LinearLayout.LayoutParams(-1, -2))
            addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        }
        setPageContent(root, title, PageTab.PROFILE, onBack = onBack)
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
        body.addView(summaryCard("Viajá Premium Mensal", "R$ 29,90 / mês", "Próxima cobrança: 15 Nov, 2026"), spaced(0, 18))
        body.addView(label("NOVO PLANO", 10, green, true), spaced(0, 8))
        body.addView(summaryCard("Viajá Premium Anual", "R$ 9,99 / mês", "Cobrança anual de R$ 119,88 · economia de 20%"), spaced(0, 18))
        body.addView(label("A alteração será aplicada imediatamente. O próximo pagamento seguirá a forma cadastrada.", 11, muted), spaced(0, 24))
        body.addView(button("Confirmar alteração", green, Color.WHITE) { showPage(successScreen("Alteração simulada", "A demonstração não altera sua assinatura."), "Tudo certo", ::showSubscription) }, spaced(12, 10))
        body.addView(button("Voltar", Color.WHITE, green, true) { showSubscription() }, spaced(0, 8))
        return screen
    }

    private fun changePlanScreen(): View {
        val (screen, body) = page()
        body.addView(label("Escolha seu plano", 22, ink, true), centered())
        body.addView(label("Selecione a opção que combina com sua próxima jornada.", 12, muted).apply { gravity = Gravity.CENTER }, spaced(8, 20))
        body.addView(summaryCard("Aventureiro", "Grátis", "Recursos básicos para planejar viagens"), spaced(0, 12))
        body.addView(summaryCard("Viajá Premium Mensal", "R$ 29,90 / mês", "Todos os recursos premium · cobrança mensal"), spaced(0, 12))
        body.addView(summaryCard("Viajá Premium Anual ✦", "R$ 9,99 / mês", "Cobrança anual de R$ 119,88 · economia de 20%"), spaced(0, 20))
        body.addView(button("Alterar agora", green, Color.WHITE) { showPage(confirmationScreen(), "Confirmar alteração", ::showSubscription) }, spaced(8, 10))
        body.addView(button("Voltar à assinatura", Color.WHITE, green, true) { showSubscription() })
        return screen
    }

    private fun paymentMethodsScreen(): View {
        val (screen, body) = page()
        body.addView(label("Seus pagamentos", 22, ink, true), centered())
        body.addView(label("Gerencie o cartão usado na assinatura Viajá Premium.", 12, muted), spaced(8, 22))
        body.addView(label("FORMA ATUAL", 10, muted, true), spaced(0, 8))
        body.addView(summaryCard("◉  Mastercard", "••••  ••••  ••••  4242", "Validade 12/28 · Preferencial"), spaced(0, 12))
        body.addView(label("OUTRAS OPÇÕES", 10, muted, true), spaced(0, 12))
        body.addView(summaryCard("＋  Adicionar cartão", "Visa, Mastercard ou Elo", "Adicione uma nova forma de pagamento"), spaced(0, 20))
        body.addView(button("Salvar forma de pagamento", green, Color.WHITE) { showPage(successScreen("Salvamento simulado", "A demonstração não salva formas de pagamento."), "Tudo certo", ::showSubscription) }, spaced(8, 10))
        body.addView(button("Voltar à assinatura", Color.WHITE, green, true) { showSubscription() })
        return screen
    }

    private fun successScreen(title: String, message: String): View {
        val (screen, body) = page()
        body.gravity = Gravity.CENTER
        body.addView(label("✓", 42, green, true).apply { gravity = Gravity.CENTER }, centered())
        body.addView(label(title, 22, ink, true).apply { gravity = Gravity.CENTER }, spaced(12, 8))
        body.addView(label(message, 13, muted).apply { gravity = Gravity.CENTER }, spaced(0, 24))
        body.addView(button("Voltar à assinatura", green, Color.WHITE) { showSubscription() })
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
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
