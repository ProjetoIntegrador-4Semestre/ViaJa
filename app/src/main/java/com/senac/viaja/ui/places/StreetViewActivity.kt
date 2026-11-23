package com.senac.viaja.ui.places

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng
import com.senac.viaja.R
import com.senac.viaja.ui.main.PAGE_TAB_EXTRA
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent

class StreetViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN)
        val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN)
        val name = intent.getStringExtra(EXTRA_NAME).orEmpty().ifBlank { "Street View" }
        val tabId = intent.getIntExtra(PAGE_TAB_EXTRA, R.id.nav_explore)
        val tab = PageTab.entries.firstOrNull { it.menuId == tabId } ?: PageTab.EXPLORE
        setPageContent(R.layout.activity_street_view, name, tab) { finish() }

        val status = findViewById<TextView>(R.id.streetViewStatus)
        if (!latitude.isFinite() || !longitude.isFinite()) {
            status.text = "Coordenadas indisponíveis para este lugar."
            return
        }

        val fragment = supportFragmentManager.findFragmentByTag("street-view") as? SupportStreetViewPanoramaFragment
            ?: SupportStreetViewPanoramaFragment.newInstance().also {
                supportFragmentManager.beginTransaction().replace(R.id.streetViewContainer, it, "street-view").commit()
            }
        fragment.getStreetViewPanoramaAsync { panorama ->
            panorama.setOnStreetViewPanoramaChangeListener {
                status.visibility = View.GONE
            }
            panorama.setPosition(LatLng(latitude, longitude), 100)
        }
    }

    companion object {
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_NAME = "name"
    }
}
