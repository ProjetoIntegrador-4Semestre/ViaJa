package com.senac.viaja.ui.main

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.senac.viaja.R
import com.senac.viaja.ui.places.AiTripChatActivity
import com.senac.viaja.ui.trips.TripListActivity

internal enum class PageTab(val menuId: Int, val destination: Class<out AppCompatActivity>) {
    HOME(R.id.nav_home, HomeActivity::class.java),
    EXPLORE(R.id.nav_explore, ExploreActivity::class.java),
    TRIPS(R.id.nav_trips, TripListActivity::class.java),
    SAVED(R.id.nav_saved, SavedItemsActivity::class.java),
    PROFILE(R.id.nav_profile, ProfileActivity::class.java)
}

internal const val PAGE_TAB_EXTRA = "page_tab"

internal fun AppCompatActivity.setPageContent(
    @LayoutRes layout: Int,
    title: CharSequence,
    tab: PageTab?,
    topLevel: Boolean = false,
    onBack: (() -> Unit)? = null
) = setPageContent(layoutInflater.inflate(layout, null), title, tab, topLevel, onBack)

internal fun AppCompatActivity.setPageContent(
    content: View,
    title: CharSequence,
    tab: PageTab?,
    topLevel: Boolean = false,
    onBack: (() -> Unit)? = null
): TextView {
    val systemBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    enableEdgeToEdge(statusBarStyle = systemBarStyle, navigationBarStyle = systemBarStyle)
    val root = layoutInflater.inflate(R.layout.page_frame, null)
    val header = root.findViewById<View>(R.id.pageHeader)
    val body = root.findViewById<FrameLayout>(R.id.pageBody)
    val footer = root.findViewById<View>(R.id.pageFooter)
    val titleView = root.findViewById<TextView>(R.id.pageTitle)
    titleView.text = title
    body.addView(content, FrameLayout.LayoutParams(-1, -1))

    val back = root.findViewById<ImageButton>(R.id.pageBack)
    back.isVisible = onBack != null
    onBack?.let { back.setOnClickListener { it() } }

    val ai = root.findViewById<ImageButton>(R.id.pageAi)
    if (this is AiTripChatActivity) {
        ai.contentDescription = "Conversa com a Vi"
        ai.isFocusable = false
    } else {
        ai.setOnClickListener {
            startActivity(Intent(this, AiTripChatActivity::class.java)
                .putExtra(PAGE_TAB_EXTRA, tab?.menuId ?: R.id.nav_explore))
        }
    }

    val nav = root.findViewById<BottomNavigationView>(R.id.bottom_nav)
    nav.isItemActiveIndicatorEnabled = false
    footer.isVisible = tab != null
    if (tab != null) {
        nav.selectedItemId = tab.menuId
        val navigate: (Int) -> Unit = { menuId ->
            val destination = PageTab.entries.first { it.menuId == menuId }
            if (!topLevel || destination != tab) {
                startActivity(Intent(this, destination.destination)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
                finish()
            }
        }
        nav.setOnItemSelectedListener {
            navigate(it.itemId)
            true
        }
        nav.setOnItemReselectedListener { navigate(it.itemId) }
    }

    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
        val footerVisible = tab != null && !insets.isVisible(WindowInsetsCompat.Type.ime())
        header.updatePadding(left = bars.left, top = bars.top, right = bars.right)
        body.updatePadding(left = bars.left, right = bars.right, bottom = if (footerVisible) 0 else maxOf(bars.bottom, keyboard.bottom))
        footer.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
        footer.isVisible = footerVisible
        WindowInsetsCompat.CONSUMED
    }
    setContentView(root)
    return titleView
}
