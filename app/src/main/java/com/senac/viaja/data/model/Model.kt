package com.senac.viaja.data.model



data class LoginRequest(
    val email: String,
    val senha: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val senha: String
)

data class ViajaUser(
    val id: Long,
    val email: String,
    val name: String?,
    val defaultMode: String? = null
)

data class LoginResponse(
    val status: String,
    val message: String,
    val user: ViajaUser?
)

data class ApiResponse(
    val status: String,
    val message: String
)



data class AuthToken(
    val token: String,
    val user: ViajaUser
)



data class Place(
    val id: String,
    val source: String?,
    val sourceId: String?,
    val name: String,
    val address: String?,
    val lat: Double?,
    val lng: Double?,
    val categories: List<String>?,
    val rating: Float?,
    val ratingCount: Int?,
    val price: Int?,
    val priceHint: String?,
    val photoUrl: String?,
    val openNow: Boolean?,
    val tags: List<String>?,
    val description: String?
)



data class Trip(
    val id: String?,
    val userId: String,
    val name: String?,
    val mode: String?,
    val startDate: String?,
    val endDate: String?,
    val totalBudgetCents: Long?,
    val status: String?
)

data class TripRequest(
    val userId: Long,
    val name: String,
    val mode: String,
    val startDate: String?,
    val endDate: String?,
    val budgetCents: Long?,
    val cityId: Long? = null
)

data class TripResponse(
    val id: String,
    val userId: String,
    val name: String,
    val mode: String,
    val startDate: String?,
    val endDate: String?,
    val totalBudgetCents: Long?,
    val status: String
)



data class SavedItem(
    val id: String?,
    val userId: String,
    val localId: String,
    val place: Place? = null
)

data class SavedLocal(
    val id: String?,
    val userId: Long,
    val localId: Long,
    val createdAt: String?
)



data class Interest(
    val id: Long?,
    val name: String?,
    val category: String?
)



data class Review(
    val id: Long?,
    val localId: Long?,
    val rating: Int?,
    val comment: String?,
    val createdAt: String?
)



data class City(
    val id: Long?,
    val name: String?,
    val state: String?,
    val country: String?,
    val imageUrl: String?
)



data class Subscription(
    val id: Long?,
    val userId: Long?,
    val plan: String?,
    val status: String?,
    val amount: Double?,
    val startedAt: String?,
    val expiresAt: String?
)



data class TripLocal(
    val tripId: Long?,
    val localId: Long?,
    val day: Int?,
    val orderIndex: Int?,
    val note: String?
)



data class TripMemo(
    val id: Long?,
    val tripId: Long?,
    val localId: Long?,
    val note: String?,
    val createdAt: String?
)



data class TripNote(
    val id: Long?,
    val tripId: Long?,
    val note: String?,
    val createdAt: String?
)



data class Media(
    val id: Long?,
    val entityType: String?,
    val entityId: Long?,
    val url: String?,
    val type: String?
)


data class SavedPage(
    @com.google.gson.annotations.SerializedName("_embedded") val embedded: SavedEmbedded?,
    val page: PageMetadata
)

data class SavedEmbedded(val savedLocals: List<SavedItem>?)
data class PageMetadata(val totalPages: Int)
data class SavePlaceRequest(val localId: Long)

data class TripPage(
    @com.google.gson.annotations.SerializedName("_embedded") val embedded: TripEmbedded?,
    val page: PageMetadata
)
data class TripEmbedded(val trips: List<Trip>?)
data class CityPage(
    @com.google.gson.annotations.SerializedName("_embedded") val embedded: CityEmbedded?,
    val page: PageMetadata
)
data class CityEmbedded(val cityList: List<City>?)
