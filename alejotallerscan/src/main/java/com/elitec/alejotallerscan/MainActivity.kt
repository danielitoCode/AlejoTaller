package com.elitec.alejotallerscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.elitec.alejotallerscan.infraestructure.presentation.OperatorScanApp
import com.elitec.alejotallerscan.infraestructure.presentation.theme.AlejoTallerTheme

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
