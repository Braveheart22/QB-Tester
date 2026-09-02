package com.qbtester.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.qbtester.app.ui.AppViewModelFactory
import com.qbtester.app.ui.navigation.QbTesterNavHost
import com.qbtester.app.ui.theme.QbTesterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as QbTesterApplication).container
        val factory = AppViewModelFactory(container)

        setContent {
            QbTesterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QbTesterNavHost(factory = factory)
                }
            }
        }
    }
}
