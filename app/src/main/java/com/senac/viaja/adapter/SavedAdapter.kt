package com.senac.viaja.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.senac.viaja.R
import com.senac.viaja.data.model.SavedItem

class SavedAdapter(
    private val items: List<SavedItem>,
    private val onClick: (SavedItem) -> Unit
) : RecyclerView.Adapter<SavedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgSaved: ImageView = view.findViewById(R.id.imgSaved)
        val chipSavedType: TextView = view.findViewById(R.id.chipSavedType)
        val tvSavedName: TextView = view.findViewById(R.id.tvSavedName)
        val tvSavedLocation: TextView = view.findViewById(R.id.tvSavedLocation)
        val btnAddTrip: MaterialButton = view.findViewById(R.id.btnAddTrip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val place = item.place
        holder.tvSavedName.text = place?.name ?: "Local ${item.localId}"
        holder.tvSavedLocation.text = place?.address ?: ""
        holder.chipSavedType.text = place?.categories?.firstOrNull() ?: "Lugar"
        holder.btnAddTrip.text = "Ver lugar"
        holder.btnAddTrip.setOnClickListener { onClick(item) }
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
