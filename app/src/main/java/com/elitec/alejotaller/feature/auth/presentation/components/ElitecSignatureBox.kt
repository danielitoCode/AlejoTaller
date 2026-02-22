package com.elitec.alejotaller.feature.auth.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CodeOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ElitecSignatureBox(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(0.8f),
                text = "Diseñado por:"
            )
            Spacer(Modifier.width(10.dp))
            Text(
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.8f),
                text = "ELITEC"
            )
            Spacer(Modifier.width(3.dp))
            Icon(
                imageVector = Icons.Default.CodeOff,
                contentDescription = "Elitec logo",
                modifier = Modifier.size(25.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(
            modifier = Modifier.padding(bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Phone logo",
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(3.dp))
            Text(
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(0.8f),
                text = "+5356628793"
            )

        }
    }
}