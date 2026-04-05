package com.bibin.opnsense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bibin.opnsense.ui.navigation.AppNavigation
import com.bibin.opnsense.ui.theme.OPNsenseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OPNsenseTheme {
                AppNavigation()
            }
        }
    }
}
