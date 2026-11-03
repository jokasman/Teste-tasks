package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.TaskScreen
import com.example.ui.TaskViewModel
import com.example.ui.theme.MinhaListaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinhaListaTheme {
                val taskViewModel: TaskViewModel = viewModel(
                    factory = TaskViewModel.factory(application)
                )
                TaskScreen(viewModel = taskViewModel)
            }
        }
    }
}
