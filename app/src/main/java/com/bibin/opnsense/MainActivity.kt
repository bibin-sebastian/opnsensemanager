package com.bibin.opnsense

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.bibin.opnsense.ui.auth.BiometricGate
import com.bibin.opnsense.ui.navigation.AppNavigation
import com.bibin.opnsense.ui.theme.OPNsenseTheme
import com.bibin.opnsense.util.BiometricHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OPNsenseTheme {
                BiometricGate(biometricHelper) {
                    AppNavigation()
                }
            }
        }
    }
}
