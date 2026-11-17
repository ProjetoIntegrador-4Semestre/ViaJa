package com.senac.viaja.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.senac.viaja.R
import com.senac.viaja.adapter.SavedAdapter
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.ui.auth.LoginActivity
import com.senac.viaja.ui.places.PlaceDetailsActivity

class SavedItemsActivity : AppCompatActivity() {
    private val auth by lazy { AuthRepository(this) }
    private lateinit var viewModel: SavedViewModel
    private lateinit var adapter: SavedAdapter
    private var category: String? = null
    private var categoryOwner: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_saved_items, getString(R.string.saved_title), PageTab.SAVED, topLevel = true)
        category = savedInstanceState?.getString("category")
        categoryOwner = savedInstanceState?.getLong("category_owner")?.takeIf { it != 0L }
        viewModel = ViewModelProvider(this)[SavedViewModel::class.java]
        adapter = SavedAdapter { item ->
            startActivity(Intent(this, PlaceDetailsActivity::class.java)
                .putExtra("place_id", item.localId)
                .putExtra(PAGE_TAB_EXTRA, R.id.nav_saved))
        }.apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        findViewById<RecyclerView>(R.id.recycler_saved).adapter = adapter
        viewModel.ui.observe(this, ::render)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("category", category)
        categoryOwner?.let { outState.putLong("category_owner", it) }
        super.onSaveInstanceState(outState)
    }

    private fun refresh() {
        val userId = auth.getUser()?.id
        if (categoryOwner != userId) category = null
        categoryOwner = userId
        viewModel.list(userId)
    }

    private fun render(ui: SavedUiState) {
        val visitor = ui.userId == null
        val categories = ui.items.flatMap { it.place?.categories.orEmpty() }
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinctBy { it.lowercase() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        if (!ui.loading && ui.error == null && !visitor && categories.none { it.equals(category, ignoreCase = true) }) {
            category = null
        }
        val visibleItems = category?.let { selected ->
            ui.items.filter { item -> item.place?.categories.orEmpty().any { it.trim().equals(selected, ignoreCase = true) } }
        } ?: ui.items
        adapter.submitList(visibleItems.takeIf { it.isNotEmpty() })

        findViewById<Chip>(R.id.savedCategory).apply {
            isVisible = categories.isNotEmpty()
            isEnabled = !ui.loading
            text = category ?: getString(R.string.explore_all_categories)
            setOnClickListener { showCategories(ui, categories) }
        }
        findViewById<LinearProgressIndicator>(R.id.savedProgress).isVisible = ui.loading
        findViewById<TextView>(R.id.tvSavedStatus).text = when {
            visitor -> getString(R.string.saved_visitor)
            ui.loading -> getString(R.string.saved_loading)
            ui.error != null -> ui.error
            ui.items.isEmpty() -> getString(R.string.saved_empty)
            else -> resources.getQuantityString(R.plurals.saved_count, visibleItems.size, visibleItems.size)
        }

        findViewById<MaterialButton>(R.id.savedAction).apply {
            isVisible = !ui.loading && (visitor || ui.error != null || ui.items.isEmpty())
            when {
                visitor -> {
                    setText(R.string.saved_sign_in)
                    setOnClickListener { startActivity(Intent(this@SavedItemsActivity, LoginActivity::class.java)) }
                }
                ui.error != null -> {
                    setText(R.string.catalog_retry)
                    setOnClickListener { refresh() }
                }
                else -> {
                    setText(R.string.saved_browse)
                    setOnClickListener {
                        startActivity(Intent(this@SavedItemsActivity, ExploreActivity::class.java))
                    }
                }
            }
        }
    }

    private fun showCategories(ui: SavedUiState, categories: List<String>) {
        val labels = listOf(getString(R.string.explore_all_categories)) + categories
        val selected = categories.indexOfFirst { it.equals(category, ignoreCase = true) } + 1
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.explore_category)
            .setSingleChoiceItems(labels.toTypedArray(), selected) { dialog, index ->
                category = categories.getOrNull(index - 1)
                dialog.dismiss()
                findViewById<RecyclerView>(R.id.recycler_saved).scrollToPosition(0)
                viewModel.ui.value?.takeIf { it.userId == ui.userId }?.let(::render)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
