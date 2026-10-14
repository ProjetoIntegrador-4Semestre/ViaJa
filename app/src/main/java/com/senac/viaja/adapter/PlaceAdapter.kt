package com.senac.viaja.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.senac.viaja.R
import com.senac.viaja.data.model.Place

class PlaceAdapter(
    private val items: List<Place>,
    private val onClick: (Place) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPlace: ImageView = view.findViewById(R.id.imgPlace)
        val btnFavorite: ImageView = view.findViewById(R.id.btnFavorite)
        val tvPlaceRating: TextView = view.findViewById(R.id.tvPlaceRating)
        val tvPlaceType: TextView = view.findViewById(R.id.tvPlaceType)
        val tvPlaceName: TextView = view.findViewById(R.id.tvPlaceName)
        val tvPlaceLocation: TextView = view.findViewById(R.id.tvPlaceLocation)
        val tvPlacePrice: TextView = view.findViewById(R.id.tvPlacePrice)
        val btnView: MaterialButton = view.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = items[position]
        holder.tvPlaceName.text = place.name
        holder.tvPlaceLocation.text = place.address ?: ""
        holder.tvPlaceType.text = (place.categories?.firstOrNull() ?: "Pousada").uppercase()
        holder.tvPlaceRating.text = if (place.rating != null) "★ ${place.rating}" else "★ --"
        holder.tvPlacePrice.text = if (place.price != null) {
            "R$ ${place.price / 100},00${place.priceHint ?: ""}"
        } else {
            "Preço sob consulta"
        }
        holder.btnView.setOnClickListener { onClick(place) }
        holder.itemView.setOnClickListener { onClick(place) }
    }

    override fun getItemCount(): Int = items.size
}
