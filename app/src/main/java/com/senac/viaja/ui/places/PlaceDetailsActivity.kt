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
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.repository.SavedRepository
import com.senac.viaja.ui.main.PlaceViewModel
import com.senac.viaja.ui.trips.TripListActivity

class PlaceDetailsActivity : AppCompatActivity() {

    private lateinit var placeViewModel: PlaceViewModel

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

        val user = AuthRepository(this).getUser()
        val localId = placeId.toLongOrNull()
        val savedRepository = SavedRepository()
        ivBookmark.isEnabled = user == null
        if (user != null && localId != null) {
            savedRepository.isSaved(user.id, localId, { saved ->
                isSaved = saved
                updateBookmarkIcon(ivBookmark)
                ivBookmark.isEnabled = true
            }, { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            })
        }
        ivBookmark.setOnClickListener {
            if (user == null || AuthRepository(this).getUser() == null) {
                startActivity(Intent(this, com.senac.viaja.ui.auth.LoginActivity::class.java))
                finish()
                return@setOnClickListener
            }
            if (localId == null) return@setOnClickListener
            ivBookmark.isEnabled = false
            val onSuccess = {
                isSaved = !isSaved
                updateBookmarkIcon(ivBookmark)
                ivBookmark.isEnabled = true
                Toast.makeText(this, if (isSaved) "Lugar salvo" else "Lugar removido dos salvos", Toast.LENGTH_SHORT).show()
            }
            val onError: (String) -> Unit = { message ->
                ivBookmark.isEnabled = true
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
            if (isSaved) savedRepository.unsave(user.id, localId, onSuccess, onError)
            else savedRepository.save(user.id, localId, onSuccess, onError)
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
                java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("pt-BR")).format(place.price / 100.0)
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
        ivBookmark.isSelected = isSaved
        ivBookmark.alpha = if (isSaved) 1f else 0.5f
        ivBookmark.contentDescription = if (isSaved) "Remover dos salvos" else "Salvar lugar"
    }
}
