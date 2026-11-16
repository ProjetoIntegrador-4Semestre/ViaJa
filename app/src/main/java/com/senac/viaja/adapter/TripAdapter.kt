package com.senac.viaja.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.senac.viaja.R
import com.senac.viaja.ui.trips.TripFormRules
import com.senac.viaja.data.model.Trip

class TripAdapter(
    private val items: List<Trip>,
    private val onClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_trip_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = items[position]
        holder.name.text = trip.name ?: "Viagem"
        holder.itemView.findViewById<TextView>(R.id.chipTripMode).text = if (trip.mode == "work") "Trabalho" else "Turismo"
        holder.itemView.findViewById<TextView>(R.id.tvTripDates).text = TripFormRules.datesLabel(trip.startDate, trip.endDate)
        holder.itemView.findViewById<TextView>(R.id.tvTripBudget).text = trip.totalBudgetCents?.let {
            java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("pt-BR")).format(it / 100.0)
        } ?: "Orçamento a definir"
        holder.itemView.findViewById<TextView>(R.id.tripCardStatus).text = when (trip.status) {
            "active" -> "Em andamento"
            "completed" -> "Concluída"
            "cancelled" -> "Cancelada"
            else -> "Planejada"
        }
        holder.itemView.setOnClickListener { onClick(trip) }
    }

    override fun getItemCount(): Int = items.size
}
