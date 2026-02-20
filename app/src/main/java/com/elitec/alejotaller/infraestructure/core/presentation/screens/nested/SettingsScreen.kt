package com.elitec.alejotaller.infraestructure.core.presentation.screens.nested

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R

@Preview(
    showBackground = true
)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var theme by rememberSaveable { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.alejoicon_clean),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
            Text(
                style = MaterialTheme.typography.headlineLarge,
                text = "Ajustes"
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(
                start = 25.dp, end = 25.dp
            )
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                text = "Tema:"
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Light mode"
                )
                Switch(
                    checked = theme,
                    onCheckedChange = {
                        theme = !theme
                    }
                )
                Icon(
                    imageVector = Icons.Default.ModeNight,
                    contentDescription = "Night mode"
                )
            }
        }
        Surface(
            shadowElevation = 3.dp,
            tonalElevation = 5.dp,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(25.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp)
            ) { }
        }
    }
}