package com.mystic.grammio.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R

/** Whether an API key is saved, as an icon and a short label. */
@Composable
fun ApiKeyStatus(hasApiKey: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (hasApiKey) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.settings_key_saved))
        } else {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text(stringResource(R.string.settings_key_missing))
        }
    }
}
