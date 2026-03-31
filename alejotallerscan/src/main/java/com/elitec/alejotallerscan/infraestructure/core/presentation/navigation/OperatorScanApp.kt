package com.elitec.alejotallerscan.infraestructure.core.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun OperatorScanApp() {
    var currentDestination by rememberSaveable(
        stateSaver = Saver(
            save = { destination -> destination.route },
            restore = { route -> OperatorDestination.fromRoute(route) }
        )
    ) {
        mutableStateOf<OperatorDestination>(OperatorDestination.Login)
    }

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
