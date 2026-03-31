package com.elitec.alejotallerscan.infraestructure.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.elitec.alejotallerscan.infraestructure.core.presentation.navigation.OperatorScanApp
import com.elitec.alejotallerscan.infraestructure.core.presentation.theme.AlejoTallerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlejoTallerTheme {
                OperatorScanApp()
            }
        }
    }
}
