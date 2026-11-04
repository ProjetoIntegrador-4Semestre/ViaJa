package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senac.viaja.R
import com.senac.viaja.adapter.SavedAdapter
import com.senac.viaja.data.model.SavedItem
import com.senac.viaja.ui.auth.AuthViewModel
import com.senac.viaja.ui.places.PlaceDetailsActivity

class SavedItemsActivity : AppCompatActivity() {

    private lateinit var viewModel: SavedViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_saved_items)

        viewModel = ViewModelProvider(this).get(SavedViewModel::class.java)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        val recycler = findViewById<RecyclerView>(R.id.recycler_saved)

        val storedUser = authViewModel.getStoredUser()
        val userId = storedUser?.id?.toString() ?: "1"

        var adapter = SavedAdapter(emptyList(), userId) { item ->
            openPlaceDetails(item.localId)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        viewModel.saved.observe(this) { items ->
            adapter = SavedAdapter(items, userId) { item ->
                openPlaceDetails(item.localId)
            }
            recycler.adapter = adapter
        }

        storedUser?.let { user ->
            viewModel.list(user.id)
        }
    }

    private fun openPlaceDetails(placeId: String) {
        val intent = Intent(this, PlaceDetailsActivity::class.java)
        intent.putExtra("place_id", placeId)
        startActivity(intent)
    }
}
