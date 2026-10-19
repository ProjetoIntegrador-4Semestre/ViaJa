package com.senac.viaja.ui.trips

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.senac.viaja.R
import com.senac.viaja.adapter.TripAdapter
import com.senac.viaja.data.mock.MockDataSource
import com.senac.viaja.ui.main.ExploreActivity
import com.senac.viaja.ui.main.ShellActivity
import com.senac.viaja.ui.main.TripViewModel

class TripListActivity : AppCompatActivity() {

    private lateinit var viewModel: TripViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trip_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this).get(TripViewModel::class.java)

        val userId = MockDataSource.currentUser.id
        val placeIdToAdd = intent.getStringExtra("add_place_id")

        val recycler = findViewById<RecyclerView>(R.id.recycler_trips)
        recycler.layoutManager = LinearLayoutManager(this)

        var adapter = TripAdapter(emptyList()) { trip ->
            openTripDashboard(trip.id ?: "")
        }
        recycler.adapter = adapter

        viewModel.trips.observe(this) { trips ->
            adapter = TripAdapter(trips) { t ->
                openTripDashboard(t.id ?: "")
            }
            recycler.adapter = adapter
        }

        viewModel.error.observe(this) { message ->
            if (message != null && message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.listByUser(userId)

        setupBottomNav()

        val btnAddTrip: MaterialButton = findViewById(R.id.btnAddTrip)
        btnAddTrip.setOnClickListener {
            showCreateTripDialog()
        }
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav?.selectedItemId = R.id.nav_trips
        nav?.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_trips) return@setOnItemSelectedListener true
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, ShellActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_saved -> {
                    val intent = Intent(this, ShellActivity::class.java)
                    intent.putExtra("tab", "saved")
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ShellActivity::class.java)
                    intent.putExtra("tab", "profile")
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun showCreateTripDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_trip, null)
        val etName: EditText = dialogView.findViewById(R.id.etTripName)
        val etMode: EditText = dialogView.findViewById(R.id.etTripMode)
        val btnConfirm: Button = dialogView.findViewById(R.id.btnConfirmCreateTrip)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancelCreateTrip)

        val alert = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConfirm.setOnClickListener {
            val name = etName.text.toString().trim()
            val mode = etMode.text.toString().trim()
            if (name.isEmpty() || mode.isEmpty()) {
                Toast.makeText(this, "Informe nome e modo da viagem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userId = MockDataSource.currentUser.id
            viewModel.createTrip(userId, name, mode)
            alert.dismiss()
        }

        btnCancel.setOnClickListener { alert.dismiss() }

        alert.show()
    }

    private fun openTripDashboard(tripId: String) {
        val intent = Intent(this@TripListActivity, TripDashboardActivity::class.java)
        intent.putExtra("trip_id", tripId)
        startActivity(intent)
    }
}
