package com.mystic.grammio.presentation.settings

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/*
 * Container transform between a settings page and the row (or button) that opens it: the row's
 * bounds grow into the page, its rounded corners flattening out, and the page shrinks back into the
 * row when it closes. Both ends are keyed by the page's SettingsRoute and matched in the
 * SharedTransitionScope that SettingsNavigation provides.
 *
 * The row and the page crossfade only while the container is small (the start of opening, the end
 * of closing). Whatever sits behind the morph is the page underneath, which is opaque, so the white
 * window never shows through.
 */

/** Null outside [SettingsNavigation], in previews for instance, where the morph does nothing. */
internal val LocalPageMorphScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** Marks the row or button that opens [route]: the page grows out of it and shrinks back into it. */
@Composable
internal fun Modifier.morphsInto(route: SettingsRoute): Modifier {
    val sharedScope = LocalPageMorphScope.current ?: return this
    val visibilityScope = LocalNavAnimatedContentScope.current
    return with(sharedScope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState(route),
            animatedVisibilityScope = visibilityScope,
            enter = fadeIn(tween(FADE_MILLIS, delayMillis = MORPH_MILLIS - FADE_MILLIS)),
            exit = fadeOut(tween(FADE_MILLIS)),
            boundsTransform = MorphBoundsTransform,
            // The row keeps its size and rides the top edge of the growing container.
            resizeMode = scaleToBounds(ContentScale.None, Alignment.TopCenter),
            clipInOverlayDuringTransition = OverlayClip(morphShape(visibilityScope, isPage = false)),
        )
    }
}

/** The page end of the morph: the whole of [route]'s page, which grows out of its row. */
@Composable
internal fun MorphingPage(
    route: SettingsRoute,
    content: @Composable () -> Unit,
) {
    val sharedScope = LocalPageMorphScope.current
    if (sharedScope == null) {
        content()
        return
    }
    val visibilityScope = LocalNavAnimatedContentScope.current
    val modifier = with(sharedScope) {
        Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(route),
            animatedVisibilityScope = visibilityScope,
            enter = fadeIn(tween(FADE_MILLIS)),
            exit = fadeOut(tween(FADE_MILLIS, delayMillis = MORPH_MILLIS - FADE_MILLIS)),
            boundsTransform = MorphBoundsTransform,
            // Laid out at full size and scaled to the container's width, top first, so the page
            // is revealed downwards rather than reflowed into a row-sized box.
            resizeMode = scaleToBounds(ContentScale.FillWidth, Alignment.TopCenter),
            clipInOverlayDuringTransition = OverlayClip(morphShape(visibilityScope, isPage = true)),
        )
    }
    Box(modifier.fillMaxSize()) { content() }
}

/**
 * Rounded like a card while the container is small, square once it fills the screen. The radius is
 * read when the clip is drawn, so animating it doesn't recompose the page.
 */
@Composable
private fun morphShape(
    visibilityScope: AnimatedVisibilityScope,
    isPage: Boolean,
): RoundedCornerShape {
    val radius = visibilityScope.transition.animateDp(
        transitionSpec = { motion() },
        label = "morph corner radius",
    ) { state ->
        val showsPage = (state == EnterExitState.Visible) == isPage
        if (showsPage) 0.dp else CARD_CORNER_RADIUS
    }
    return remember(radius) {
        RoundedCornerShape(
            object : CornerSize {
                override fun toPx(
                    shapeSize: Size,
                    density: Density,
                ) = with(density) { radius.value.toPx() }
            },
        )
    }
}

private val MorphBoundsTransform = BoundsTransform { _, _ -> motion() }

/** Material's emphasized easing: a quick start that settles slowly into place. */
private fun <T> motion(): FiniteAnimationSpec<T> = tween(MORPH_MILLIS, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))

/** Also the length of the whole page transition, see [SettingsTransitions]. */
internal const val MORPH_MILLIS = 450
private const val FADE_MILLIS = 150
private val CARD_CORNER_RADIUS = 16.dp
