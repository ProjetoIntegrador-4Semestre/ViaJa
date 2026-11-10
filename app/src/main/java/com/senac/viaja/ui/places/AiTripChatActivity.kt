package com.senac.viaja.ui.places

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.senac.viaja.R
import com.senac.viaja.ui.main.PAGE_TAB_EXTRA
import com.senac.viaja.ui.main.PageTab
import com.senac.viaja.ui.main.setPageContent
import com.senac.viaja.adapter.ChatMessageAdapter
import com.senac.viaja.data.model.ChatMessage

class AiTripChatActivity : AppCompatActivity() {

    private lateinit var etInput: TextInputEditText
    private lateinit var btnSend: com.google.android.material.button.MaterialButton
    private lateinit var recycler: RecyclerView

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatMessageAdapter
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tabId = intent.getIntExtra(PAGE_TAB_EXTRA, R.id.nav_explore)
        val tab = PageTab.entries.firstOrNull { it.menuId == tabId } ?: PageTab.EXPLORE
        setPageContent(R.layout.activity_ai_trip_chat, "Conversa com a Vi", tab) { finish() }

        etInput = findViewById(R.id.etAiInput)
        btnSend = findViewById(R.id.btnAiSend)
        recycler = findViewById(R.id.recycler_ai_chat)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = ChatMessageAdapter(messages)
        recycler.adapter = adapter

        val tripName = intent.getStringExtra("trip_name").orEmpty()
        val tripMode = when (intent.getStringExtra("trip_mode")) {
            "work" -> "Trabalho"
            "tourism" -> "Turismo"
            else -> ""
        }
        val context = listOf(tripName, tripMode).filter { it.isNotBlank() }.joinToString(" · ")
        findViewById<android.widget.TextView>(R.id.tvAiContext).text = context.ifBlank { "Planejamento de viagem" }
        val greeting = if (tripName.isBlank()) {
            "Oi! Sou a Vi, sua assistente de viagem. O que você gostaria de planejar?"
        } else {
            "Oi! Sou a Vi, sua assistente de viagem. Como posso ajudar com $tripName?"
        }
        addMessage("Vi", greeting, isUser = false)

        btnSend.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            addMessage("Você", text, isUser = true)
            etInput.setText("")
            recycler.scrollToPosition(messages.lastIndex)

            // Resposta simulada da IA (viria da API real)
            handler.postDelayed({
                val resposta = gerarRespostaIA(text, tripName.ifBlank { "sua viagem" }, tripMode.ifBlank { "planejamento livre" })
                addMessage("Vi", resposta, isUser = false)
                recycler.scrollToPosition(messages.lastIndex)
            }, 1000)
        }
    }

    private fun addMessage(autor: String, texto: String, isUser: Boolean) {
        val ts = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        messages.add(ChatMessage(text = "$autor: $texto", isUser = isUser, timestamp = ts))
        adapter.notifyDataSetChanged()
    }

    private fun gerarRespostaIA(pergunta: String, tripName: String, tripMode: String): String {
        return when {
            pergunta.contains("roteiro", ignoreCase = true) -> {
                "Aqui está um roteiro sugerido para $tripName ($tripMode): 1) Café da manhã no hotel, 2) Visita aos pontos turísticos principais, 3) Almoço em restaurante local, 4) Passeio da tarde, 5) Jantar. Quer que eu detalhe algum dia?"
            }
            pergunta.contains("hotel|hospedagem|pousada", ignoreCase = true) -> {
                "Para $tripName, recomendo hospedagem bem avaliada perto dos pontos principais. Quer que eu busque opções?"
            }
            pergunta.contains("restaurante|comer|comida", ignoreCase = true) -> {
                "Existem ótimos restaurantes locais. Posso sugerir alguns com boa avaliação se quiser."
            }
            else -> {
                "Recebi sua solicitação: '$pergunta'. Estou processando com base no contexto da sua viagem a $tripName ($tripMode). Em breve terei sugestões para você."
            }
        }
    }
}
