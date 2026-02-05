package com.elitec.alejotaller.infraestructure.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

@Composable
fun MainNvigationWrapper(
    modifier: Modifier = Modifier
) {
    val backStack = remember { mutableStateListOf<MainRoutesKey>(MainRoutesKey.Splash) }
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when(key) {
                is MainRoutesKey.Home -> TODO()
                MainRoutesKey.Landing -> TODO()
                MainRoutesKey.Login -> TODO()
                MainRoutesKey.Splash -> TODO()
                else -> NavEntry(MainRoutesKey.Error) {
                    Text(text = "Error")
                }
            }
        }
    )
}