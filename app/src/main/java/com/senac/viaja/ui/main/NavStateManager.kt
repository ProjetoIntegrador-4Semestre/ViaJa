package com.senac.viaja.ui.main

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment

object NavStateManager {

    private const val PREFS = "nav_state"
    private const val KEY_PREFIX = "group_"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun saveFragment(context: Context, group: String, fragmentClassName: String) {
        prefs(context).edit().putString("$KEY_PREFIX$group", fragmentClassName).apply()
    }

    fun getLastFragmentClassName(context: Context, group: String): String? {
        return prefs(context).getString("$KEY_PREFIX$group", null)
    }

    fun clearGroup(context: Context, group: String) {
        prefs(context).edit().remove("$KEY_PREFIX$group").apply()
    }
}
