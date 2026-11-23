package com.senac.viaja.ui.places

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.senac.viaja.R
import com.senac.viaja.ui.main.PAGE_TAB_EXTRA
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.data.model.Place
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.repository.SavedRepository
import com.senac.viaja.ui.main.PlaceViewModel
import com.senac.viaja.ui.trips.TripListActivity

class PlaceDetailsActivity : AppCompatActivity() {

    private lateinit var placeViewModel: PlaceViewModel

    private var currentPlace: Place? = null
    private var isSaved = false
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tabId = intent.getIntExtra(PAGE_TAB_EXTRA, R.id.nav_explore)
        val tab = PageTab.entries.firstOrNull { it.menuId == tabId } ?: PageTab.EXPLORE
        val headerTitle = setPageContent(R.layout.activity_place_details, "Lugar", tab) { finish() }

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

        val mapFragment = supportFragmentManager.findFragmentByTag("place-map") as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().replace(R.id.imgPlaceMap, it, "place-map").commit()
            }
        mapFragment.getMapAsync {
            googleMap = it
            renderMap()
        }

        val user = AuthRepository(this).getUser()
        val localId = placeId.toLongOrNull()
        val savedRepository = SavedRepository()
        ivBookmark.visibility = if (localId == null) View.GONE else View.VISIBLE
        btnAddToTrip.visibility = if (localId == null) View.GONE else View.VISIBLE
        chipRating.visibility = if (localId == null) View.GONE else View.VISIBLE
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

        btnConsultSite.visibility = View.GONE
        btnConsultSite.setOnClickListener {
            val url = currentPlace?.website ?: currentPlace?.googleMapsUri ?: return@setOnClickListener
            try {
                startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
            } catch (_: android.content.ActivityNotFoundException) {
                Toast.makeText(this, "Nenhum navegador disponível.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.imgHero).visibility=View.GONE
        placeViewModel.place.observe(this) { place ->
            CatalogImages.show(findViewById(R.id.imgHero),place.photoUrl)
            chipRating.text="Avaliar"
            currentPlace = place
            val source = when(place.source){"mock","demo"->"catálogo demonstrativo; confirme os dados antes de viajar";"google"->"Google Places";null->"catálogo do Viajá";else->place.source}
            val hours = place.openingHours?.takeIf { it.isNotBlank() }?.let { "\n\nHorários\n$it" }.orEmpty()
            tvPlaceDescription.text = place.description.orEmpty()+hours+"\n\nFonte: "+source
            val externalUrl = place.website ?: place.googleMapsUri
            val website = externalUrl?.let(android.net.Uri::parse)
            btnConsultSite.visibility = if (website?.scheme == "https" && !website.host.isNullOrBlank()) View.VISIBLE else View.GONE
            btnConsultSite.text = if (place.website == null && place.googleMapsUri != null) "Abrir no Maps" else "Consultar site"
            headerTitle.text = place.name
            tvPlaceName.text = place.name
            tvPlaceRating.text = place.rating?.toString() ?: "Sem avaliações"
            tvPlaceRatingCount.text = if (place.ratingCount != null) "(${place.ratingCount})" else "(0)"
            tvPlaceCity.text = place.address ?: ""
            chipType.text = place.categories?.firstOrNull() ?: "Local"
            chipRating.text = "Avaliar"
            tvPlacePrice.text = if (place.price != null) {
                java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("pt-BR")).format(place.price / 100.0)
            } else if (!place.priceHint.isNullOrBlank()) {
                "Faixa de preço: ${place.priceHint}"
            } else {
                "Preço sob consulta"
            }
            renderMap()
        }

        placeViewModel.error.observe(this) { message ->
            if (message != null && message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        findViewById<View>(R.id.tvPlaceName).setOnLongClickListener {
            if(AuthRepository(this).getUser()==null){startActivity(Intent(this,com.senac.viaja.ui.auth.LoginActivity::class.java))}
            else com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Avaliar este lugar")
                .setItems(arrayOf("1 estrela","2 estrelas","3 estrelas","4 estrelas","5 estrelas")){_,index->
                    com.senac.viaja.data.remote.ApiClient.service.rate(placeId,com.senac.viaja.data.model.RatingWrite(index+1)).enqueue(object:retrofit2.Callback<com.senac.viaja.data.model.RatingResult>{
                        override fun onResponse(call:retrofit2.Call<com.senac.viaja.data.model.RatingResult>,response:retrofit2.Response<com.senac.viaja.data.model.RatingResult>){Toast.makeText(this@PlaceDetailsActivity,if(response.isSuccessful)"Avaliação salva" else "Não foi possível salvar a avaliação",Toast.LENGTH_SHORT).show();if(response.isSuccessful)placeViewModel.getPlace(placeId)}
                        override fun onFailure(call:retrofit2.Call<com.senac.viaja.data.model.RatingResult>,failure:Throwable){Toast.makeText(this@PlaceDetailsActivity,"Não foi possível confirmar a avaliação",Toast.LENGTH_SHORT).show()}
                    })
                }.show()
            true
        }
        chipRating.text="Avaliar"
        chipRating.setOnClickListener {findViewById<View>(R.id.tvPlaceName).performLongClick()}
        listOf(R.id.tvAmenitiesTitle,R.id.amenitiesRow1,R.id.amenitiesRow2).forEach{findViewById<View>(it).visibility=View.GONE}
        findViewById<android.widget.TextView>(R.id.tvPriceLabel).text="Valor do catálogo · confira a unidade"
        findViewById<android.widget.TextView>(R.id.tvPlaceRatingCount).apply{contentDescription="Segure o nome do lugar para avaliar"}
        placeViewModel.getPlace(placeId)
    }

    private fun updateBookmarkIcon(ivBookmark: ImageView) {
        ivBookmark.setImageResource(if (isSaved) R.drawable.ic_bookmark else R.drawable.ic_bookmark)
        ivBookmark.isSelected = isSaved
        ivBookmark.alpha = if (isSaved) 1f else 0.5f
        ivBookmark.contentDescription = if (isSaved) "Remover dos salvos" else "Salvar lugar"
    }

    private fun renderMap() {
        val map = googleMap ?: return
        val place = currentPlace ?: return
        val latitude = place.lat
        val longitude = place.lng
        val container = findViewById<View>(R.id.imgPlaceMap)
        if (latitude == null || longitude == null || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            container.visibility = View.GONE
            return
        }
        container.visibility = View.VISIBLE
        val position = LatLng(latitude, longitude)
        map.clear()
        map.addMarker(MarkerOptions().position(position).title(place.name).snippet(place.address))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
        map.uiSettings.isMapToolbarEnabled = true
    }
}
