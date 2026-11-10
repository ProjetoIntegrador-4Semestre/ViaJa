package com.senac.viaja.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.senac.viaja.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageContent(R.layout.activity_main, "Início", PageTab.HOME, topLevel = true)
    }
}
