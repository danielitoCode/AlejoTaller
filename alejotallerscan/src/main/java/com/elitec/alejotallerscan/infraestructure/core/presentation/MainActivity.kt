package com.elitec.alejotallerscan.infraestructure.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.elitec.alejotallerscan.infraestructure.core.presentation.navigation.OperatorScanApp
import com.elitec.alejotallerscan.infraestructure.core.presentation.theme.AlejoTallerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlejoTallerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPaddings ->
                    OperatorScanApp(
                        modifier = Modifier.fillMaxSize()
                            .padding(innerPaddings)
                    )
                }
            }
        }
    }
}
