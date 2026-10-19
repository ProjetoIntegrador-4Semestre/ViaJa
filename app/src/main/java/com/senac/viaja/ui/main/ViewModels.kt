package com.senac.viaja.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.senac.viaja.data.model.City
import com.senac.viaja.data.model.Place
import com.senac.viaja.data.model.SavedItem
import com.senac.viaja.data.model.Subscription
import com.senac.viaja.data.model.Trip
import com.senac.viaja.data.model.TripResponse
import com.senac.viaja.data.model.ViajaUser
import com.senac.viaja.data.repository.CityRepository
import com.senac.viaja.data.repository.PlaceCallback
import com.senac.viaja.data.repository.PlaceRepository
import com.senac.viaja.data.repository.SavedRepository
import com.senac.viaja.data.repository.SubscriptionRepository
import com.senac.viaja.data.repository.TripRepository
import com.senac.viaja.data.repository.UserRepository
import com.senac.viaja.data.remote.ApiClient

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TripRepository()

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> = _trips

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadTrips() {
        _loading.value = true
        repository.list(
            { trips ->
                _loading.value = false
                _trips.value = trips
            },
            { message ->
                _loading.value = false
                _error.value = message
            }
        )
    }
}

class PlaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlaceRepository()

    private val _places = MutableLiveData<List<Place>>()
    val places: LiveData<List<Place>> = _places

    private val _place = MutableLiveData<Place>()
    val place: LiveData<Place> = _place

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun search(query: String) {
        _loading.value = true
        repository.search(query, object : PlaceCallback {
            override fun onSuccess(places: List<Place>) {
                _loading.value = false
                _places.value = places
            }

            override fun onPlace(place: Place) {}

            override fun onError(message: String) {
                _loading.value = false
                _error.value = message
            }
        })
    }

    fun getPlace(id: String) {
        _loading.value = true
        repository.getPlace(id, object : PlaceCallback {
            override fun onSuccess(places: List<Place>) {}

            override fun onPlace(place: Place) {
                _loading.value = false
                _place.value = place
            }

            override fun onError(message: String) {
                _loading.value = false
                _error.value = message
            }
        })
    }

    fun searchByCity(city: String) {
        _loading.value = true
        repository.searchByCity(city, object : PlaceCallback {
            override fun onSuccess(places: List<Place>) {
                _loading.value = false
                _places.value = places
            }

            override fun onPlace(place: Place) {}

            override fun onError(message: String) {
                _loading.value = false
                _error.value = message
            }
        })
    }
}

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TripRepository()

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> = _trips

    private val _trip = MutableLiveData<TripResponse>()
    val trip: LiveData<TripResponse> = _trip

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun listByUser(userId: Long) {
        _loading.value = true
        repository.listByUser(userId,
            { trips ->
                _loading.value = false
                _trips.value = trips
            },
            { message ->
                _loading.value = false
                _error.value = message
            }
        )
    }

    fun getById(id: Long) {
        _loading.value = true
        repository.getById(id,
            { any ->
                _loading.value = false
            },
            { message ->
                _loading.value = false
                _error.value = message
            }
        )
    }

    fun createTrip(userId: Long, name: String, mode: String) {
        _loading.value = true
        repository.create(
            com.senac.viaja.data.model.TripRequest(userId, name, mode, null, null, null),
            { trip ->
                _loading.value = false
                _trip.value = trip
            },
            { message ->
                _loading.value = false
                _error.value = message
            }
        )
    }
}

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository()

    private val _user = MutableLiveData<ViajaUser>()
    val user: LiveData<ViajaUser> = _user

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getById(id: Long) {
        _loading.value = true
        repository.getById(id,
            { user ->
                _loading.value = false
                _user.value = user
            },
            { message ->
                _loading.value = false
                _error.value = message
            }
        )
    }

    fun update(id: Long, name: String, email: String,
               onSuccess: () -> Unit,
               onError: (String) -> Unit) {
        repository.update(id, name, email, onSuccess, onError)
    }
}

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriptionRepository()

    private val _subscription = MutableLiveData<Subscription>()
    val subscription: LiveData<Subscription> = _subscription

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getByUser(userId: Long) {
        _loading.value = true
        repository.getByUser(userId,
            { sub ->
                _loading.value = false
                _subscription.value = sub
            },
            { message ->
                _loading.value = false
                _error.value = message
            }
        )
    }
}

class SavedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SavedRepository()

    private val _saved = MutableLiveData<List<SavedItem>>()
    val saved: LiveData<List<SavedItem>> = _saved

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun list(userId: Long) {
        _loading.value = true
        repository.list(userId,
            { items ->
                _loading.value = false
                _saved.value = items
            },
            { message ->
                _loading.value = false
                _error.value = message
            }
        )
    }

    fun save(userId: Long, localId: Long,
             onSuccess: () -> Unit,
             onError: (String) -> Unit) {
        repository.save(userId, localId, onSuccess, onError)
    }

    fun unsave(userId: Long, localId: Long,
               onSuccess: () -> Unit,
               onError: (String) -> Unit) {
        repository.unsave(userId, localId, onSuccess, onError)
    }
}

class CityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CityRepository()

    private val _cities = MutableLiveData<List<City>>()
    val cities: LiveData<List<City>> = _cities

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun list() {
        _loading.value = true
        repository.list(
            { cities ->
                _loading.value = false
                _cities.value = cities
            },
            { message ->
                _loading.value = false
                _error.value = message
            }
        )
    }
}
