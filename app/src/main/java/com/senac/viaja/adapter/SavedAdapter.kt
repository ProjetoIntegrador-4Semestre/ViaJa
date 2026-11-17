package com.senac.viaja.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.senac.viaja.R
import com.senac.viaja.data.model.SavedItem

class SavedAdapter(
    private val onClick: (SavedItem) -> Unit
) : ListAdapter<SavedItem, SavedAdapter.ViewHolder>(SavedDiff) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val category: TextView = view.findViewById(R.id.chipSavedType)
        val name: TextView = view.findViewById(R.id.tvSavedName)
        val address: TextView = view.findViewById(R.id.tvSavedLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_saved_card, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val place = item.place
        val context = holder.itemView.context
        holder.name.text = place?.name ?: context.getString(R.string.saved_place)
        holder.address.text = if (place == null) context.getString(R.string.saved_place_unavailable) else place.address
        holder.address.isVisible = !holder.address.text.isNullOrBlank()
        val category = place?.categories?.firstOrNull { it.isNotBlank() }
        holder.category.text = category
        holder.category.isVisible = category != null
        holder.itemView.setOnClickListener { onClick(item) }
    }

    private object SavedDiff : DiffUtil.ItemCallback<SavedItem>() {
        override fun areItemsTheSame(oldItem: SavedItem, newItem: SavedItem) =
            oldItem.userId == newItem.userId && oldItem.localId == newItem.localId

        override fun areContentsTheSame(oldItem: SavedItem, newItem: SavedItem) = oldItem == newItem
    }
}
