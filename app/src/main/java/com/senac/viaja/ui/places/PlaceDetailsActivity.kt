package com.senac.viaja.ui.places

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.senac.viaja.R
import com.senac.viaja.data.model.Place
import com.senac.viaja.data.mock.MockDataSource
import com.senac.viaja.ui.main.PlaceViewModel
import com.senac.viaja.ui.main.SavedViewModel
import com.senac.viaja.ui.trips.TripListActivity

class PlaceDetailsActivity : AppCompatActivity() {

    private lateinit var placeViewModel: PlaceViewModel
    private lateinit var savedViewModel: SavedViewModel

    private var currentPlace: Place? = null
    private var isSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_place_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        placeViewModel = ViewModelProvider(this).get(PlaceViewModel::class.java)
        savedViewModel = ViewModelProvider(this).get(SavedViewModel::class.java)

        val placeId = intent.getStringExtra("place_id") ?: ""

        val ivBookmark: ImageView = findViewById(R.id.ivBookmark)
        val btnAddToTrip: MaterialButton = findViewById(R.id.btnAddToTrip)
        val btnConsultSite: MaterialButton = findViewById(R.id.btnConsultSite)

        val tvPlaceName = findViewById<android.widget.TextView>(R.id.tvPlaceName)
        val tvPlaceRating = findViewById<android.widget.TextView>(R.id.tvPlaceRating)
        val tvPlaceRatingCount = findViewById<android.widget.TextView>(R.id.tvPlaceRatingCount)
        val tvPlaceCity = findViewById<android.widget.TextView>(R.id.tvPlaceCity)
        val tvPlaceDescription = findViewById<android.widget.TextView>(R.id.tvPlaceDescription)
        val tvPlacePrice = findViewById<android.widget.TextView>(R.id.tvPlacePrice)
        val chipType = findViewById<Chip>(R.id.chipType)
        val chipRating = findViewById<Chip>(R.id.chipRating)

        // Usuário logado (dado dinâmico do mock/API)
        val userId = MockDataSource.currentUser.id.toString()

        // Verifica se já está salvo
        isSaved = MockDataSource.isSaved(userId, placeId)
        updateBookmarkIcon(ivBookmark)

        ivBookmark.setOnClickListener {
            isSaved = !isSaved
            updateBookmarkIcon(ivBookmark)
            val message = if (isSaved) "Salvo nos favoritos" else "Removido dos favoritos"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            // Persiste no "banco" (mock) — em produção chama a API real
            if (isSaved) {
                savedViewModel.save(userId.toLong(), placeId.toLong(),
                    { }, { err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show() }
                )
            } else {
                savedViewModel.unsave(userId.toLong(), placeId.toLong(),
                    { }, { err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show() }
                )
            }
        }

        btnAddToTrip.setOnClickListener {
            val intent = Intent(this@PlaceDetailsActivity, TripListActivity::class.java)
            intent.putExtra("add_place_id", placeId)
            startActivity(intent)
        }

        btnConsultSite.setOnClickListener {
            Toast.makeText(this, "Abrindo site do parceiro (simulado)", Toast.LENGTH_SHORT).show()
        }

        placeViewModel.place.observe(this) { place ->
            currentPlace = place
            tvPlaceName.text = place.name
            tvPlaceRating.text = if (place.rating != null) "${place.rating}" else "0.0"
            tvPlaceRatingCount.text = if (place.ratingCount != null) "(${place.ratingCount})" else "(0)"
            tvPlaceCity.text = place.address ?: ""
            chipType.text = place.categories?.firstOrNull() ?: "Tipo"
            chipRating.text = if (place.rating != null) "${place.rating}" else "Avaliação"
            tvPlaceDescription.text = place.description ?: ""
            tvPlacePrice.text = if (place.price != null) {
                "R$ ${place.price / 100},00 ${place.priceHint ?: ""}"
            } else {
                "Preço sob consulta"
            }
        }

        placeViewModel.error.observe(this) { message ->
            if (message != null && message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        placeViewModel.getPlace(placeId)
    }

    private fun updateBookmarkIcon(ivBookmark: ImageView) {
        ivBookmark.setImageResource(if (isSaved) R.drawable.ic_bookmark else R.drawable.ic_bookmark)
        // Em produção, usaria um drawable preenchido quando salvo
    }
}
