package com.senac.viaja.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.senac.viaja.R
import com.senac.viaja.data.model.ChatMessage

class ChatMessageAdapter(private val items: List<ChatMessage>) : RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bubble: LinearLayout = view.findViewById(R.id.chatBubble)
        val author: TextView = view.findViewById(R.id.chatAuthor)
        val text: TextView = view.findViewById(R.id.tv_chat_text)
        val meta: TextView = view.findViewById(R.id.chatMeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = items[position]
        val gravity = if (message.isUser) Gravity.END else Gravity.START
        (holder.itemView as LinearLayout).gravity = gravity
        holder.bubble.setBackgroundResource(if (message.isUser) R.drawable.vi_bubble_user else R.drawable.vi_bubble_assistant)
        val color = ContextCompat.getColor(holder.itemView.context, if (message.isUser) R.color.white else R.color.vi_ink)
        holder.author.text = if (message.isUser) "Você" else if(message.delivery=="local") "Vi · Apresentação" else "Vi"
        holder.author.setTextColor(color)
        holder.text.setTextColor(color)
        holder.text.text = message.text
        val width = holder.itemView.resources.displayMetrics.widthPixels
        holder.text.maxWidth = (width * 0.74).toInt()
        holder.meta.text = if(message.isUser && message.delivery!="sent") "${message.timestamp} · ${if(message.delivery=="sending")"Enviando…" else "Não confirmado"}" else message.timestamp
    }

    override fun getItemCount() = items.size
}
