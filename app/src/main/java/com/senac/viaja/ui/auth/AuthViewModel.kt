package com.senac.viaja.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.senac.viaja.data.model.AuthToken
import com.senac.viaja.data.model.ViajaUser
import com.senac.viaja.data.repository.AuthRepository

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _user = MutableLiveData<ViajaUser>()
    val user: LiveData<ViajaUser> = _user

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    fun login(email: String, password: String) {
        _loading.value = true
        _error.value = null
        repository.login(email, password,
            { token ->
                _loading.value = false
                _user.value = token.user
            },
            { message ->
                _loading.value = false
                _error.value = message
            }
        )
    }

    fun register(name: String, email: String, password: String) {
        _loading.value = true
        _error.value = null
        repository.register(name, email, password,
            {
                _loading.value = false
                _registerSuccess.value = true
            },
            { message ->
                _loading.value = false
                _error.value = message
            }
        )
    }

    fun getStoredUser(): ViajaUser? {
        return repository.getUser()
    }

    fun getToken(): String? {
        return repository.getToken()
    }

    fun logout() {
        repository.clearSession()
        _user.value = null
    }

    fun updateStoredUser(nome: String, email: String) {
        repository.updateStoredUser(nome, email)
        _user.value = repository.getUser()
    }
}
