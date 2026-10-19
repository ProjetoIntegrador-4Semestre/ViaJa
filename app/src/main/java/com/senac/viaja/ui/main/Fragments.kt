package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.senac.viaja.R
import com.senac.viaja.adapter.CityAdapter
import com.senac.viaja.adapter.PlaceAdapter
import com.senac.viaja.adapter.SavedAdapter
import com.senac.viaja.data.model.City
import com.senac.viaja.data.model.Place
import com.senac.viaja.data.model.SavedItem
import com.senac.viaja.data.mock.MockDataSource
import com.senac.viaja.ui.auth.AuthViewModel
import com.senac.viaja.ui.places.AiTripChatActivity
import com.senac.viaja.ui.places.PlaceDetailsActivity
import com.senac.viaja.ui.places.PlaceListActivity
import com.senac.viaja.ui.profile.EditProfileActivity
import com.senac.viaja.ui.main.SavedViewModel
import com.senac.viaja.ui.main.UserViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: CityViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(CityViewModel::class.java)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        val tvGreeting = view.findViewById<TextView>(R.id.tvHomeGreeting)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etHomeSearch)
        val btnExplore = view.findViewById<MaterialButton>(R.id.btnExploreHome)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_destinations)

        // Saudação com nome do usuário logado (dado dinâmico da API/mock)
        val storedUser = authViewModel.getStoredUser()
        tvGreeting.text = if (storedUser?.name != null) "Olá, ${storedUser.name}!" else "Olá, Viajante!"

        var adapter = CityAdapter(emptyList()) { city ->
            openPlaceList(requireContext(), city.name ?: "")
        }
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter

        viewModel.cities.observe(viewLifecycleOwner) { cities ->
            adapter = CityAdapter(cities) { c ->
                openPlaceList(requireContext(), c.name ?: "")
            }
            recycler.adapter = adapter
        }

        btnExplore.setOnClickListener {
            val q = etSearch.text?.toString()?.trim()?.ifEmpty { "" } ?: ""
            openPlaceList(requireContext(), q)
        }

        return view
    }

    private fun openPlaceList(context: android.content.Context, query: String) {
        val intent = Intent(context, PlaceListActivity::class.java)
        intent.putExtra("q", query)
        context.startActivity(intent)
    }
}

class SavedItemsFragment : Fragment() {

    private lateinit var viewModel: SavedViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_items, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(SavedViewModel::class.java)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_saved)

        val storedUser = authViewModel.getStoredUser()
        val userId = storedUser?.id?.toString() ?: "1"

        var adapter = SavedAdapter(emptyList(), userId) { item ->
            openPlaceDetails(requireContext(), item.localId)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        viewModel.saved.observe(viewLifecycleOwner) { items ->
            adapter = SavedAdapter(items, userId) { item ->
                openPlaceDetails(requireContext(), item.localId)
            }
            recycler.adapter = adapter
        }

        storedUser?.let { user ->
            viewModel.list(user.id)
        }

        return view
    }

    private fun openPlaceDetails(context: android.content.Context, placeId: String) {
        val intent = Intent(context, PlaceDetailsActivity::class.java)
        intent.putExtra("place_id", placeId)
        context.startActivity(intent)
    }
}

class ProfileFragment : Fragment() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        val tvName = view.findViewById<TextView>(R.id.tv_profile_name)

        val storedUser = authViewModel.getStoredUser()
        tvName.text = storedUser?.name ?: "Usuário"
        storedUser?.let { userViewModel.getById(it.id) }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            tvName.text = user.name ?: "Usuário"
        }

        val btnEditProfile = view.findViewById<MaterialButton>(R.id.btn_edit_profile)
        btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
