package com.mystic.grammio.presentation.process.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mystic.grammio.R
import com.mystic.grammio.presentation.process.ResultUiState

@Composable
fun ResultCard(
    result: ResultUiState,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val isFailure = result is ResultUiState.Failure
    val containerColor by animateColorAsState(
        if (isFailure) colors.errorContainer else colors.surfaceContainerHigh,
        label = "resultContainer",
    )
    val contentColor by animateColorAsState(
        if (isFailure) colors.onErrorContainer else colors.onSurface,
        label = "resultContent",
    )

    Surface(
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        AnimatedContent(
            targetState = result,
            contentKey = { it::class },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentAlignment = Alignment.TopStart,
            label = "result",
        ) { current ->
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                when (current) {
                    ResultUiState.Idle -> IdleHint()

                    ResultUiState.Loading -> LoadingPlaceholder()

                    is ResultUiState.Success -> SelectionContainer {
                        Text(current.text, style = MaterialTheme.typography.bodyLarge)
                    }

                    is ResultUiState.Failure -> ErrorContent(current.error, onRetry, onOpenSettings)
                }
            }
        }
    }
}

@Composable
private fun IdleHint() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            stringResource(R.string.process_pick_transformation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Pulsing text-shaped bars, so the card keeps roughly the size the result will have. */
@Composable
private fun LoadingPlaceholder() {
    val pulse by rememberInfiniteTransition(label = "placeholder").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 700), RepeatMode.Reverse),
        label = "pulse",
    )
    val barColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    Column(Modifier.alpha(pulse), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(1f, 0.92f, 0.6f).forEach { width ->
            Box(
                Modifier
                    .fillMaxWidth(width)
                    .height(14.dp)
                    .background(barColor, CircleShape),
            )
        }
    }
}
