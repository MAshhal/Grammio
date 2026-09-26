package com.mystic.grammio.presentation.settings.provider.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R

/** Whether an API key is saved, as a small tinted pill. */
@Composable
fun ApiKeyStatus(hasApiKey: Boolean) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (hasApiKey) colors.secondaryContainer else colors.errorContainer,
        contentColor = if (hasApiKey) colors.onSecondaryContainer else colors.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                if (hasApiKey) Icons.Outlined.CheckCircle else Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                stringResource(if (hasApiKey) R.string.settings_key_saved else R.string.settings_key_missing),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
