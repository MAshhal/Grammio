package com.mystic.grammio.presentation.process.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.presentation.common.message
import com.mystic.grammio.presentation.process.model.ErrorRecovery
import com.mystic.grammio.presentation.process.model.recovery

/** The error message plus the one recovery action that makes sense for it, if any. */
@Composable
fun ErrorContent(
    error: TransformError,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null)
            Text(error.message())
        }
        when (error.recovery) {
            ErrorRecovery.Retry -> RecoveryButton(Icons.Outlined.Refresh, R.string.action_retry, onRetry)

            ErrorRecovery.OpenSettings ->
                RecoveryButton(Icons.Outlined.Settings, R.string.action_open_settings, onOpenSettings)

            ErrorRecovery.None -> Unit
        }
    }
}

@Composable
private fun RecoveryButton(
    icon: ImageVector,
    @StringRes label: Int,
    onClick: () -> Unit,
) {
    // Sits on the error container, so it follows that container's content color.
    val contentColor = LocalContentColor.current
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.5f)),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(stringResource(label))
    }
}
