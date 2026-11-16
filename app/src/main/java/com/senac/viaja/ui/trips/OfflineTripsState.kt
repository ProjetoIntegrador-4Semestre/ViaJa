package com.senac.viaja.ui.trips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.senac.viaja.data.model.TripBundle
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.repository.OfflineRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineTripsState(app: Application) : AndroidViewModel(app) {
    val changed = MutableLiveData(0)
    val owner = AuthRepository(app).offlineOwner()
    var items = emptyList<TripBundle>()
        private set
    var busy = false
        private set
    var loaded = false
        private set
    var error: String? = null
        private set
    var selectedId: String? = null

    fun notifyChanged() {
        changed.value = (changed.value ?: 0) + 1
    }

    private fun currentAccount() = owner != null && AuthRepository(getApplication()).offlineOwner() == owner

    fun load() {
        if (busy) return
        if (!currentAccount()) {
            items = emptyList()
            error = "Entre na conta para consultar suas viagens baixadas."
            notifyChanged()
            return
        }
        busy = true
        error = null
        notifyChanged()
        viewModelScope.launch {
            try {
                val downloads = withContext(Dispatchers.IO) {
                    OfflineRepository(getApplication(), owner).list()
                }
                if (currentAccount()) {
                    items = downloads
                    loaded = true
                    if (items.none { it.trip.id == selectedId }) selectedId = null
                } else {
                    items = emptyList()
                    error = "A conta mudou. Abra os downloads novamente."
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                error = "Não foi possível ler os downloads. Tente novamente."
            } finally {
                busy = false
                notifyChanged()
            }
        }
    }

    fun remove(id: String) {
        if (busy || !currentAccount()) return
        busy = true
        error = null
        notifyChanged()
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    OfflineRepository(getApplication(), owner).remove(id)
                }
                items = if (currentAccount()) items.filterNot { it.trip.id == id } else emptyList()
                if (selectedId == id) selectedId = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                error = "Não foi possível remover o download. Tente novamente."
            } finally {
                busy = false
                notifyChanged()
            }
        }
    }
}
