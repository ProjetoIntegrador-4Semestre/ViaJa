package com.senac.viaja.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.senac.viaja.data.model.DiscoveryOptions
import com.senac.viaja.data.model.Place
import com.senac.viaja.data.remote.ApiClient
import com.senac.viaja.data.remote.deliver
import com.senac.viaja.data.repository.PlaceCallback
import com.senac.viaja.data.repository.PlaceRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal data class DiscoveryFilters(
    val city: Long? = null,
    val category: String? = null,
    val maxPrice: Long? = null,
    val minRating: Float? = null,
    val sort: String = "name"
)

internal data class DiscoveryUiState(
    val options: DiscoveryOptions? = null,
    val query: String = "",
    val filters: DiscoveryFilters = DiscoveryFilters(),
    val places: List<Place> = emptyList(),
    val total: Long = 0,
    val hasMore: Boolean = false,
    val busy: Boolean = true,
    val initialized: Boolean = false,
    val legacyCatalog: Boolean = false,
    val error: String? = null
)

internal class DiscoveryState : ViewModel() {
    private val changes = MutableLiveData(DiscoveryUiState())
    val ui: LiveData<DiscoveryUiState> = changes
    val current get() = checkNotNull(changes.value)
    private var initializing = false
    private var nextPage = 0
    private var failedPage = 0

    fun initialize() {
        if (current.initialized || initializing) return
        initializing = true
        changes.value = current.copy(busy = true, error = null)
        ApiClient.service.discoveryOptions().enqueue(object : Callback<DiscoveryOptions> {
            override fun onResponse(call: Call<DiscoveryOptions>, response: Response<DiscoveryOptions>) {
                val options = response.body()
                when {
                    response.isSuccessful && options != null -> startCatalog(options, legacy = false)
                    response.code() == 404 || response.code() == 405 -> startCatalog(null, legacy = true)
                    else -> optionsFailed("Não foi possível carregar o catálogo.")
                }
            }

            override fun onFailure(call: Call<DiscoveryOptions>, t: Throwable) {
                optionsFailed("Não foi possível conectar ao catálogo.")
            }
        })
    }

    fun search(query: String, filters: DiscoveryFilters = current.filters) {
        if (!current.initialized || current.busy) return
        nextPage = 0
        loadPage(0, current.copy(query = query.trim(), filters = filters, places = emptyList(), total = 0, hasMore = false))
    }

    fun loadMore() {
        if (current.hasMore && !current.busy && current.error == null) loadPage(nextPage)
    }

    fun retry() {
        if (current.busy || current.error == null) return
        if (current.initialized) loadPage(failedPage) else initialize()
    }

    private fun startCatalog(options: DiscoveryOptions?, legacy: Boolean) {
        initializing = false
        loadPage(0, current.copy(options = options, legacyCatalog = legacy, initialized = true))
    }

    private fun optionsFailed(message: String) {
        initializing = false
        changes.value = current.copy(busy = false, error = message)
    }

    private fun loadPage(page: Int, request: DiscoveryUiState = current) {
        changes.value = request.copy(busy = true, error = null)

        fun completed(items: List<Place>, total: Long, hasMore: Boolean) {
            nextPage = page + 1
            changes.value = current.copy(
                places = if (page == 0) items else (request.places + items).distinctBy(Place::id),
                total = total,
                hasMore = hasMore,
                busy = false
            )
        }

        fun failed(message: String) {
            failedPage = page
            changes.value = current.copy(busy = false, error = message)
        }

        if (request.legacyCatalog) {
            PlaceRepository().search(request.query, object : PlaceCallback {
                override fun onSuccess(places: List<Place>) = completed(places, places.size.toLong(), false)
                override fun onPlace(place: Place) = Unit
                override fun onError(message: String) = failed(message)
            })
        } else {
            val filters = request.filters
            ApiClient.service.discover(
                request.query, filters.city, filters.category, filters.maxPrice, filters.minRating, filters.sort, page
            ).deliver({ completed(it.places, it.total, it.hasMore) }, ::failed)
        }
    }
}
