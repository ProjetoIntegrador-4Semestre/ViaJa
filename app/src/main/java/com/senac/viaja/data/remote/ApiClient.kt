package com.senac.viaja.data.remote

import com.senac.viaja.data.model.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

object ApiClient {
    private val BASE_URL = com.senac.viaja.BuildConfig.API_BASE_URL

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
            val token = AuthSession.tokenProvider?.invoke()
            if (token != null && token.isNotEmpty()) {
                builder.header("Authorization", "Bearer $token")
            }
            val response = chain.proceed(builder.build())
            if (response.code() == 401 && token != null) AuthSession.onUnauthorized?.invoke(token)
            response
        }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    object AuthSession {
        var onUnauthorized: ((String) -> Unit)? = null
        var tokenProvider: (() -> String?)? = null
    }
}

interface ApiService {


    @POST("auth/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

    @POST("auth/signup")
    fun signup(@Body body: SignupRequest): Call<ApiResponse>


    @GET("users")
    fun getUsers(): Call<Any>

    @GET("users/{id}")
    fun getUserById(@Path("id") id: Long): Call<ViajaUser>

    @PUT("users/{id}")
    fun updateUser(@Path("id") id: Long, @Body body: Any): Call<Any>

    @DELETE("users/{id}")
    fun deleteUser(@Path("id") id: Long): Call<Any>


    @GET("places/search")
    fun searchPlaces(@Query("q") q: String?): Call<List<Place>>

    @GET("places")
    fun getPlaces(@Query("size") size: Int): Call<CatalogPlacePage>

    @GET("places/{id}")
    fun getPlace(@Path("id") id: String): Call<Place>

    @POST("places")
    fun createPlace(@Body body: Any): Call<Any>

    @PUT("places/{id}")
    fun updatePlace(@Path("id") id: String, @Body body: Any): Call<Any>

    @DELETE("places/{id}")
    fun deletePlace(@Path("id") id: String): Call<Any>


    @GET("trips")
    fun getTrips(@Query("page") page: Int = 0): Call<TripPage>

    @GET("trips/user/{userId}")
    fun getTripsByUser(@Path("userId") userId: Long, @Query("page") page: Int = 0): Call<TripPage>

    @POST("trips")
    fun createTrip(@Body request: TripRequest): Call<TripResponse>

    @GET("trips/{id}")
    fun getTripById(@Path("id") id: Long): Call<TripResponse>

    @PUT("trips/{id}")
    fun updateTrip(@Path("id") id: Long, @Body request: TripRequest): Call<TripResponse>

    @DELETE("trips/{id}")
    fun deleteTrip(@Path("id") id: Long): Call<Void>


    @GET("users/{userId}/saved-locals")
    fun getSavedLocals(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int? = null,
        @Query("sort") sort: String? = null
    ): Call<SavedPage>

    @GET("users/{userId}/saved-locals/{localId}")
    fun getSavedLocal(@Path("userId") userId: Long, @Path("localId") localId: Long): Call<SavedItem>

    @POST("users/{userId}/saved-locals")
    fun saveLocal(@Path("userId") userId: Long, @Body body: SavePlaceRequest): Call<Void>

    @DELETE("users/{userId}/saved-locals/{localId}")
    fun unsaveLocal(@Path("userId") userId: Long, @Path("localId") localId: Long): Call<Void>


    @GET("interests")
    fun getInterests(): Call<Any>

    @GET("interests/{id}")
    fun getInterestById(@Path("id") id: Long): Call<Any>

    @POST("interests")
    fun createInterest(@Body body: Any): Call<Any>

    @PUT("interests/{id}")
    fun updateInterest(@Path("id") id: Long, @Body body: Any): Call<Any>

    @DELETE("interests/{id}")
    fun deleteInterest(@Path("id") id: Long): Call<Any>


    @GET("cities")
    fun getCities(@Query("page") page: Int = 0): Call<CityPage>

    @GET("cities/{id}")
    fun getCityById(@Path("id") id: Long): Call<City>

    @POST("cities")
    fun createCity(@Body body: Any): Call<Any>

    @PUT("cities/{id}")
    fun updateCity(@Path("id") id: Long, @Body body: Any): Call<Any>

    @DELETE("cities/{id}")
    fun deleteCity(@Path("id") id: Long): Call<Any>


    @GET("locals/{localId}/reviews")
    fun getReviewsByLocal(@Path("localId") localId: Long): Call<Any>

    @POST("locals/{localId}/reviews")
    fun createReview(@Path("localId") localId: Long, @Body body: Any): Call<Any>

    @PUT("locals/{localId}/reviews/{id}")
    fun updateReview(@Path("localId") localId: Long, @Path("id") id: Long, @Body body: Any): Call<Any>

    @DELETE("locals/{localId}/reviews/{id}")
    fun deleteReview(@Path("localId") localId: Long, @Path("id") id: Long): Call<Any>


    @GET("subscriptions/users/{userId}")
    fun getSubscriptionByUser(@Path("userId") userId: Long): Call<Any>

    @POST("subscriptions")
    fun createSubscription(@Body body: Any): Call<Any>

    @PUT("subscriptions/{id}")
    fun updateSubscription(@Path("id") id: Long, @Body body: Any): Call<Any>

    @DELETE("subscriptions/{id}")
    fun deleteSubscription(@Path("id") id: Long): Call<Any>


    @POST("trip-locals")
    fun addTripLocal(@Body stop: TripLocal): Call<TripLocal>

    @GET("trip-locals/trip/{tripId}")
    fun getTripLocalsByTrip(@Path("tripId") tripId: Long): Call<List<TripLocal>>

    @POST("trip-locals")
    fun createTripLocal(@Body body: Any): Call<Any>

    @PUT("trip-locals/{tripId}/{localId}")
    fun updateTripLocal(@Path("tripId") tripId: Long, @Path("localId") localId: Long, @Body body: Any): Call<Any>

    @PUT("trip-locals/{tripId}/{localId}/schedule")
    fun updateStopSchedule(@Path("tripId") tripId: Long, @Path("localId") localId: Long, @Body schedule: StopSchedule): Call<TripLocal>

    @DELETE("trip-locals/{tripId}/{localId}")
    fun deleteTripLocal(@Path("tripId") tripId: Long, @Path("localId") localId: Long): Call<Any>


    @GET("trips/{tripId}/notes")
    fun getTripNotes(@Path("tripId") tripId: Long, @Query("sort") sort: String = "id,desc"): Call<TripNotesPage>

    @POST("trips/{tripId}/notes")
    fun createTripNote(@Path("tripId") tripId: Long, @Body body: Any): Call<Any>

    @PUT("trips/{tripId}/notes/{id}")
    fun updateTripNote(@Path("tripId") tripId: Long, @Path("id") id: Long, @Body body: Any): Call<Any>

    @DELETE("trips/{tripId}/notes/{id}")
    fun deleteTripNote(@Path("tripId") tripId: Long, @Path("id") id: Long): Call<Any>


    @GET("trip-memos/trip-locals/{tripId}/{localId}/memos")
    fun getTripMemos(@Path("tripId") tripId: Long, @Path("localId") localId: Long): Call<List<TripMemo>>

    @POST("trip-memos")
    fun createTripMemo(@Body body: Any): Call<Any>

    @PUT("trip-memos/{id}")
    fun updateTripMemo(@Path("id") id: Long, @Body body: Any): Call<Any>

    @DELETE("trip-memos/{id}")
    fun deleteTripMemo(@Path("id") id: Long): Call<Any>


    @GET("medias")
    fun getMedias(): Call<Any>

    @POST("medias")
    fun createMedia(@Body body: Any): Call<Any>

    @PUT("medias/{id}")
    fun updateMedia(@Path("id") id: Long, @Body body: Any): Call<Any>

    @DELETE("medias/{id}")
    fun deleteMedia(@Path("id") id: Long): Call<Any>


    @GET("users/{userId}/interests")
    fun getUserInterests(@Path("userId") userId: Long): Call<Any>

    @POST("users/{userId}/interests")
    fun addUserInterest(@Path("userId") userId: Long, @Body body: Any): Call<Any>

    @DELETE("users/{userId}/interests/{interestId}")
    fun removeUserInterest(@Path("userId") userId: Long, @Path("interestId") interestId: Long): Call<Any>


    @GET("locals/{localId}/interests")
    fun getLocalInterests(@Path("localId") localId: Long): Call<Any>

    @POST("locals/{localId}/interests")
    fun addLocalInterest(@Path("localId") localId: Long, @Body body: Any): Call<Any>

    @DELETE("locals/{localId}/interests/{interestId}")
    fun removeLocalInterest(@Path("localId") localId: Long, @Path("interestId") interestId: Long): Call<Any>
}
