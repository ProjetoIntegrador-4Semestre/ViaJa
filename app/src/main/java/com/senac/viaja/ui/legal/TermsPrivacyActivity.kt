package com.senac.viaja.ui.legal

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar
import com.senac.viaja.R

class TermsPrivacyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transparent = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(transparent, transparent),
            navigationBarStyle = SystemBarStyle.light(transparent, transparent)
        )
        setContentView(R.layout.activity_terms_privacy)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.termsPrivacyRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        findViewById<MaterialToolbar>(R.id.termsPrivacyToolbar).setNavigationOnClickListener { finish() }

        val body = findViewById<LinearLayout>(R.id.termsPrivacyBody)
        val sections = listOf(
            R.string.terms_use_title to R.string.terms_use_body,
            R.string.terms_premium_title to R.string.terms_premium_body,
            R.string.privacy_account_title to R.string.privacy_account_body,
            R.string.privacy_device_title to R.string.privacy_device_body,
            R.string.privacy_services_title to R.string.privacy_services_body,
            R.string.privacy_controls_title to R.string.privacy_controls_body
        )
        val headingFont = ResourcesCompat.getFont(this, R.font.plus_jakarta_sans_bold)
        sections.forEach { (title, content) ->
            body.addView(TextView(this).apply {
                setText(title)
                setTextColor(getColor(R.color.viaja_text))
                textSize = 18f
                typeface = headingFont
                ViewCompat.setAccessibilityHeading(this, true)
            })
            body.addView(TextView(this).apply {
                setText(content)
                setTextColor(getColor(R.color.viaja_gray))
                textSize = 15f
                setPadding(0, (6 * resources.displayMetrics.density).toInt(), 0, (20 * resources.displayMetrics.density).toInt())
            })
        }
    }
}
