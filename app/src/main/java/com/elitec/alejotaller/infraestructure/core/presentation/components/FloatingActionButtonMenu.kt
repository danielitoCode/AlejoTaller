package com.elitec.alejotaller.infraestructure.core.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.infraestructure.core.presentation.navigation.InternalRoutesKey
import com.elitec.alejotaller.infraestructure.core.presentation.uiModels.FabMenuItem

@Composable
fun FloatingActionButtonMenu(
    shopCartItemsCount: Int,
    items: List<FabMenuItem>,
    onLogout: () -> Unit,
    onNavigate: (InternalRoutesKey) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {

        // Overlay (doc: bloquea fondo al expandir)
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                    .clickable { expanded = false }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() +
                        slideInVertically { it / 2 } +
                        scaleIn(initialScale = 0.8f),
                exit = fadeOut() +
                        slideOutVertically { it / 2 } +
                        scaleOut(targetScale = 0.8f)
            ) {

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items.forEach { item ->
                        FabMenuItemComponent(
                            item = item,
                            shopCartItemsCount = shopCartItemsCount,
                            onClick = {
                                if (item.route == InternalRoutesKey.Logout) {
                                    onLogout()
                                    return@FabMenuItemComponent
                                }
                                expanded = false
                                onNavigate(item.route)
                            }
                        )
                    }
                }
            }
            BadgedBox(
                badge = {
                    if(shopCartItemsCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(text = shopCartItemsCount.toString())
                        }
                    }
                }
            ) {
                FloatingActionButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.Menu,
                        contentDescription = null
                    )
                }
            }
        }
    }
}