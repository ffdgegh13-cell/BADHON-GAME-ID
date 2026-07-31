package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.TempMailRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.TempMailTheme
import com.example.ui.viewmodel.TempMailViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TempMailRepository(database.tempMailDao())
        val viewModelFactory = TempMailViewModel.Factory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[TempMailViewModel::class.java]

        setContent {
            TempMailTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
