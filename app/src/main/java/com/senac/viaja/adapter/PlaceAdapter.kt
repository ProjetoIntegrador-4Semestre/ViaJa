package com.senac.viaja.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.senac.viaja.R
import com.senac.viaja.data.model.Place
import com.senac.viaja.ui.places.CatalogImages
import java.text.NumberFormat
import java.util.Locale

class PlaceAdapter(
    items: List<Place>,
    private val onClick: (Place) -> Unit
) : ListAdapter<Place, PlaceAdapter.ViewHolder>(PlaceDiff) {
    private val currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))

    init {
        submitList(items)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgPlace)
        val rating: TextView = view.findViewById(R.id.tvRating)
        val category: TextView = view.findViewById(R.id.tvPlaceType)
        val name: TextView = view.findViewById(R.id.tvPlaceName)
        val address: TextView = view.findViewById(R.id.tvPlaceLocation)
        val price: TextView = view.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_place_card, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = getItem(position)
        val context = holder.itemView.context
        CatalogImages.show(holder.image, place.photoUrl)
        holder.name.text = place.name
        holder.address.text = place.address
        holder.address.isVisible = !place.address.isNullOrBlank()
        val category = place.categories?.firstOrNull { it.isNotBlank() }
        holder.category.text = category
        holder.category.isVisible = category != null
        holder.rating.text = place.rating?.let { "★ " + it.toString().replace('.', ',') }
            ?: context.getString(R.string.catalog_no_ratings)
        holder.price.text = place.price?.let {
            context.getString(R.string.catalog_price, currency.format(it / 100.0))
        }
        holder.price.isVisible = place.price != null
        holder.itemView.setOnClickListener { onClick(place) }
    }

    private object PlaceDiff : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Place, newItem: Place) = oldItem == newItem
    }
}
