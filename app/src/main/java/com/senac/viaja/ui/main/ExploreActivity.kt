package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.senac.viaja.R
import com.senac.viaja.adapter.PlaceAdapter
import com.senac.viaja.data.model.DiscoveryOptions
import com.senac.viaja.ui.places.PlaceDetailsActivity
import com.senac.viaja.ui.trips.TripFormRules

class ExploreActivity : AppCompatActivity() {
    private lateinit var state: DiscoveryState
    private lateinit var query: TextInputEditText
    private lateinit var results: RecyclerView
    private lateinit var adapter: PlaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_explore, getString(R.string.explore_title), PageTab.EXPLORE, topLevel = true)

        state = ViewModelProvider(this)[DiscoveryState::class.java]
        query = findViewById(R.id.exploreQuery)
        query.setText(state.current.query)
        query.setOnEditorActionListener { _, action, _ ->
            if (action != EditorInfo.IME_ACTION_SEARCH) return@setOnEditorActionListener false
            submitSearch()
            true
        }
        findViewById<Chip>(R.id.exploreSearch).setOnClickListener { submitSearch() }
        findViewById<Chip>(R.id.exploreFilters).setOnClickListener {
            state.current.options?.let(::showFilters)
        }

        results = findViewById(R.id.exploreResults)
        adapter = PlaceAdapter(emptyList()) { place ->
            startActivity(Intent(this, PlaceDetailsActivity::class.java)
                .putExtra("place_id", place.id)
                .putExtra(PAGE_TAB_EXTRA, R.id.nav_explore))
        }.apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        results.adapter = adapter

        state.ui.observe(this, ::render)
        state.initialize()
    }

    private fun submitSearch(filters: DiscoveryFilters = state.current.filters) {
        if (!state.current.initialized || state.current.busy) return
        WindowInsetsControllerCompat(window, query).hide(WindowInsetsCompat.Type.ime())
        query.clearFocus()
        results.scrollToPosition(0)
        state.search(query.text.toString(), filters)
    }

    private fun render(ui: DiscoveryUiState) {
        adapter.submitList(ui.places.takeIf { it.isNotEmpty() })
        findViewById<LinearProgressIndicator>(R.id.exploreProgress).isVisible = ui.busy
        findViewById<Chip>(R.id.exploreSearch).isEnabled = ui.initialized && !ui.busy
        findViewById<Chip>(R.id.exploreFilters).apply {
            isVisible = ui.options != null
            isEnabled = !ui.busy
            val active = listOf(ui.filters.city, ui.filters.category, ui.filters.maxPrice, ui.filters.minRating)
                .count { it != null } + if (ui.filters.sort != "name") 1 else 0
            text = if (active == 0) getString(R.string.explore_filters) else getString(R.string.explore_filters_active, active)
        }

        val summary = when {
            ui.error != null -> ui.error
            ui.busy -> getString(R.string.explore_loading)
            ui.places.isEmpty() -> getString(R.string.explore_empty)
            else -> resources.getQuantityString(R.plurals.explore_result_count, if (ui.total == 1L) 1 else 2, ui.total)
        }
        findViewById<TextView>(R.id.exploreStatus).text = if (ui.legacyCatalog) {
            getString(R.string.explore_legacy_status, summary)
        } else summary

        findViewById<MaterialButton>(R.id.exploreResultsAction).apply {
            isVisible = !ui.busy && (ui.hasMore || ui.error != null)
            setText(if (ui.error != null) R.string.catalog_retry else R.string.explore_more)
            setOnClickListener { if (ui.error != null) state.retry() else state.loadMore() }
        }
    }

    private fun showFilters(options: DiscoveryOptions) {
        val filters = state.current.filters
        val content = layoutInflater.inflate(R.layout.dialog_explore_filters, null)
        val cities = options.cities.filter { it.id != null && !it.name.isNullOrBlank() }
        val city = content.findViewById<Spinner>(R.id.filterCity)
        city.bind(listOf(getString(R.string.explore_all_cities)) + cities.map { it.name.orEmpty() },
            cities.indexOfFirst { it.id == filters.city } + 1)

        val category = content.findViewById<Spinner>(R.id.filterCategory)
        category.bind(listOf(getString(R.string.explore_all_categories)) + options.types,
            options.types.indexOf(filters.category) + 1)

        val ratings = listOf(null, 3f, 4f, 4.5f)
        val rating = content.findViewById<Spinner>(R.id.filterRating)
        rating.bind(resources.getStringArray(R.array.explore_rating_choices).toList(), ratings.indexOf(filters.minRating))

        val sorts = listOf("name", "rating", "price")
        val sort = content.findViewById<Spinner>(R.id.filterSort)
        sort.bind(resources.getStringArray(R.array.explore_sort_choices).toList(), sorts.indexOf(filters.sort))

        val price = content.findViewById<TextInputLayout>(R.id.filterPrice)
        val priceInput = content.findViewById<TextInputEditText>(R.id.filterPriceInput)
        priceInput.keyListener = DigitsKeyListener.getInstance("0123456789,.")
        priceInput.setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        priceInput.setText(TripFormRules.budgetInput(filters.maxPrice))

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.explore_filters)
            .setView(content)
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.explore_clear_filters) { _, _ -> submitSearch(DiscoveryFilters()) }
            .setPositiveButton(R.string.explore_apply_filters, null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val rawPrice = priceInput.text.toString().trim()
            val priceInReais = if (decimalPointPrice.matches(rawPrice)) rawPrice.replace('.', ',') else rawPrice
            val maxPrice = try {
                TripFormRules.validate("Filtro", "", "", priceInReais).budgetCents
            } catch (_: IllegalArgumentException) {
                price.error = getString(R.string.explore_price_error)
                priceInput.requestFocus()
                return@setOnClickListener
            }
            dialog.dismiss()
            submitSearch(DiscoveryFilters(
                city = cities.getOrNull(city.selectedItemPosition - 1)?.id,
                category = options.types.getOrNull(category.selectedItemPosition - 1),
                maxPrice = maxPrice,
                minRating = ratings[rating.selectedItemPosition],
                sort = sorts[sort.selectedItemPosition]
            ))
        }
    }

    private fun Spinner.bind(labels: List<String>, selected: Int) {
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, labels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        setSelection(selected.coerceAtLeast(0))
    }

    private companion object {
        val decimalPointPrice = Regex("[0-9]+\\.[0-9]{1,2}")
    }
}
