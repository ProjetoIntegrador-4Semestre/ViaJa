package com.senac.viaja.data.mock

import android.os.Handler
import android.os.Looper
import com.senac.viaja.data.model.*
import java.util.*

/**
 * Simula o backend. Tudo que vem da API (nomes de cidades, lugares, viagens, usuário,
 * assinatura, salvos) está aqui. A UI estática (cores, fontes, layout) continua nos XML.
 */
object MockDataSource {

    private val handler = Handler(Looper.getMainLooper())

    // ---------------- Usuário logado (vem da API) ----------------
    val currentUser = ViajaUser(
        id = 1L,
        email = "elena.rodriguez@example.com",
        name = "Elena Rodriguez",
        defaultMode = "Turismo"
    )

    // ---------------- Cidades (vem da API) ----------------
    val cities = listOf(
        City(1L, "Rio de Janeiro", "RJ", "Brasil", null),
        City(2L, "São Paulo", "SP", "Brasil", null),
        City(3L, "Salvador", "BA", "Brasil", null),
        City(4L, "Florianópolis", "SC", "Brasil", null),
        City(5L, "Buenos Aires", null, "Argentina", null),
        City(6L, "Amazonas", null, "Brasil", null)
    )

    // ---------------- Lugares (vem da API) ----------------
    val places = listOf(
        Place(
            id = "1", source = "mock", sourceId = "p1",
            name = "Pousada Maria Bonita", address = "Fernando de Noronha, PE",
            lat = -3.8545, lng = -32.4254,
            categories = listOf("Pousada", "Bem avaliada"),
            rating = 4.9f, ratingCount = 124,
            price = 180000, priceHint = "/noite",
            photoUrl = null, openNow = true,
            tags = listOf("Wi-Fi", "Piscina", "Ar Condicionado"),
            description = "Viva o autêntico modernismo tropical. Em meio à vegetação exuberante, a Pousada Maria Bonita oferece um refúgio tranquilo e ensolarado."
        ),
        Place(
            id = "2", source = "mock", sourceId = "p2",
            name = "Hotel Fasano Rio", address = "Ipanema, RJ",
            lat = -22.9895, lng = -43.2135,
            categories = listOf("Luxury Hotel"),
            rating = 4.8f, ratingCount = 89,
            price = 240000, priceHint = "/noite",
            photoUrl = null, openNow = true,
            tags = listOf("Wi-Fi", "Piscina", "Spa"),
            description = "Hotel de luxo em frente à praia de Ipanema, com atendimento premiado e vista para o mar."
        ),
        Place(
            id = "3", source = "mock", sourceId = "p3",
            name = "Copacabana Palace", address = "Copacabana, RJ",
            lat = -22.9714, lng = -43.1838,
            categories = listOf("Historic Resort"),
            rating = 4.9f, ratingCount = 203,
            price = 310000, priceHint = "/noite",
            photoUrl = null, openNow = true,
            tags = listOf("Wi-Fi", "Piscina", "Restaurante"),
            description = "Ícone da hotelaria carioca desde 1923, com arquitetura clássica e localização privilegiada."
        ),
        Place(
            id = "4", source = "mock", sourceId = "p4",
            name = "Leblon Ocean View", address = "Leblon, RJ",
            lat = -22.9849, lng = -43.2319,
            categories = listOf("Apartment"),
            rating = 4.7f, ratingCount = 56,
            price = 85000, priceHint = "/noite",
            photoUrl = null, openNow = false,
            tags = listOf("Wi-Fi", "Cozinha", "Varanda"),
            description = "Apartamento moderno com vista para o oceano, ideal para estadias longas."
        ),
        Place(
            id = "5", source = "mock", sourceId = "p5",
            name = "Casa Sol Nascente", address = "Trancoso, Bahia",
            lat = -16.6333, lng = -39.1333,
            categories = listOf("Acomodação"),
            rating = 4.6f, ratingCount = 72,
            price = 65000, priceHint = "/noite",
            photoUrl = null, openNow = true,
            tags = listOf("Wi-Fi", "Praia"),
            description = "Charme bahiano com vista para o mar, perto do Quadrado de Trancoso."
        ),
        Place(
            id = "6", source = "mock", sourceId = "p6",
            name = "Restaurante Maré", address = "Rio de Janeiro, RJ",
            lat = -22.9567, lng = -43.1734,
            categories = listOf("Restaurante"),
            rating = 4.5f, ratingCount = 112,
            price = 45000, priceHint = "/refeição",
            photoUrl = null, openNow = true,
            tags = listOf("Comida Brasileira", "Frutos do Mar"),
            description = "Cozinha brasileira contemporânea com frutos do mar frescos."
        ),
        Place(
            id = "7", source = "mock", sourceId = "p7",
            name = "Lançóis Maranhenses", address = "Maranhão",
            lat = -2.5483, lng = -43.3944,
            categories = listOf("Ponto Turístico"),
            rating = 4.8f, ratingCount = 94,
            price = 0, priceHint = "grátis",
            photoUrl = null, openNow = true,
            tags = listOf("Natureza", "Patrimônio"),
            description = "Dunas de areia branca e lagoas de água doce em um cenário único no Brasil."
        ),
        Place(
            id = "8", source = "mock", sourceId = "p8",
            name = "Café Botânico", address = "São Paulo, SP",
            lat = -23.5565, lng = -46.6534,
            categories = listOf("Restaurante", "Café"),
            rating = 4.4f, ratingCount = 67,
            price = 28000, priceHint = "/refeição",
            photoUrl = null, openNow = true,
            tags = listOf("Café da Manhã", "Wi-Fi"),
            description = "Café aconchegante em meio ao verde, ideal para trabalhar ou relaxar."
        )
    )

    // ---------------- Viagens do usuário (vem da API) ----------------
    val trips = listOf(
        Trip("t1", "1", "Rio de Janeiro", "Turismo", "05 Out", "12 Out", 240000, "ativa"),
        Trip("t2", "1", "São Paulo", "Trabalho", "15 Nov", "20 Nov", 85000, "próxima"),
        Trip("t3", "1", "Salvador", "Turismo", null, null, null, "passada")
    )

    // ---------------- Itens salvos do usuário (vem da API) ----------------
    val savedItems = listOf(
        SavedItem("s1", "1", "1"),   // Pousada Maria Bonita
        SavedItem("s2", "1", "5")    // Casa Sol Nascente
    )

    // ---------------- Assinatura (vem da API) ----------------
    val subscription = Subscription(
        id = 1L, userId = 1L, plan = "Premium", status = "ativo",
        amount = 29.90, startedAt = "15 Out, 2023", expiresAt = "15 Nov, 2023"
    )

    // ---------------- Simulação de rede (delay) ----------------
    private const val DELAY_MS = 800L

    fun <T> simular(dados: T, callback: (T) -> Unit) {
        handler.postDelayed({ callback(dados) }, DELAY_MS)
    }

    fun simularErro(callback: (String) -> Unit, mensagem: String = "Erro simulado da API") {
        handler.postDelayed({ callback(mensagem) }, DELAY_MS)
    }

    // ---------------- Consultas ----------------
    fun getPlaceById(id: String): Place? = places.find { it.id == id }

    fun searchPlaces(query: String): List<Place> {
        val q = query.lowercase()
        return places.filter {
            (it.name ?: "").lowercase().contains(q) ||
                (it.address ?: "").lowercase().contains(q) ||
                (it.tags?.joinToString(" ") ?: "").lowercase().contains(q)
        }
    }

    fun getPlacesByCity(city: String): List<Place> {
        val c = city.lowercase()
        return places.filter {
            (it.address ?: "").lowercase().contains(c) ||
                (it.tags?.joinToString(" ") ?: "").lowercase().contains(c)
        }
    }

    fun getTripById(id: String): Trip? = trips.find { it.id == id }

    fun isSaved(userId: String, localId: String): Boolean =
        savedItems.any { it.userId == userId && it.localId == localId }

    fun toggleSave(userId: String, localId: String): Boolean {
        val index = savedItems.indexOfFirst { it.userId == userId && it.localId == localId }
        return if (index >= 0) {
            // removido (não mutamos a lista imutável, apenas sinalizamos)
            false
        } else {
            true
        }
    }
}
