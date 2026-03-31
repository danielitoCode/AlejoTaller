package com.elitec.alejotallerscan.infraestructure.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.elitec.alejotallerscan.infraestructure.presentation.navigation.OperatorDestination
import com.elitec.alejotallerscan.infraestructure.presentation.navigation.OperatorNavHost

@Composable
fun OperatorScanApp() {
    var currentDestination by rememberSaveable { mutableStateOf<OperatorDestination>(OperatorDestination.Login) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        OperatorNavHost(
            currentDestination = currentDestination,
            onNavigate = { currentDestination = it }
        )
    }
}
