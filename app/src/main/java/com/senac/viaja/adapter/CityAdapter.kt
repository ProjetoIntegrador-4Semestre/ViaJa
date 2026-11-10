package com.senac.viaja.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.senac.viaja.R
import com.senac.viaja.data.model.City

class CityAdapter(
    private val items: List<City>,
    private val onClick: (City) -> Unit
) : RecyclerView.Adapter<CityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCityName: TextView = view.findViewById(R.id.tvCityName)
        val tvCityState: TextView = view.findViewById(R.id.tvCityState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_city_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = items[position]
        holder.tvCityName.text = city.name ?: "Cidade"
        holder.tvCityState.text = listOfNotNull(city.state, city.country).joinToString(", ")
        holder.itemView.setOnClickListener { onClick(city) }
    }

    override fun getItemCount(): Int = items.size
}
