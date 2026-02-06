package com.elitec.alejotaller.infraestructure.core.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.product.presentation.screen.IconPlaceholder

@Composable
fun BottomNavigationBar(modifier: Modifier = Modifier) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { IconPlaceholder(modifier = Modifier.size(24.dp), color = Color(0xFF4CAF50)) },
            label = { Text(stringResource(id = R.string.home), color = Color(0xFF4CAF50)) },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF4CAF50),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { IconPlaceholder(modifier = Modifier.size(24.dp), color = Color.Gray) },
            label = { Text(stringResource(id = R.string.search), color = Color.Gray) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { IconPlaceholder(modifier = Modifier.size(24.dp), color = Color.Gray) },
            label = { Text(stringResource(id = R.string.favorites), color = Color.Gray) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { IconPlaceholder(modifier = Modifier.size(24.dp), color = Color.Gray) },
            label = { Text(stringResource(id = R.string.profile), color = Color.Gray) },
            selected = false,
            onClick = { }
        )
    }
}