package com.mystic.grammio.presentation.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

/**
 * Page transitions for the Settings back stack. The motion itself is the container transform in
 * PageMorph.kt, drawn in the shared-transition overlay where these transitions don't reach. They
 * only keep the page underneath in place while the morph runs, and give a page with nothing to morph
 * into (a transformation that was just deleted) a zoom out instead.
 *
 * The page underneath never moves or fades, so the window behind it can't show through.
 */
internal object SettingsTransitions {

    /** Opening a page. */
    val push: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        (fadeIn(motion()) + scaleIn(motion(), initialScale = FALLBACK_SCALE)) togetherWith
            ExitTransition.KeepUntilTransitionsFinished
    }

    /** Back button or back arrow. */
    val pop: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        EnterTransition.None togetherWith (fadeOut(motion()) + scaleOut(motion(), targetScale = FALLBACK_SCALE))
    }

    /** Back gesture: the same as [pop], scrubbed by the finger, so the page shrinks back into its row. */
    val predictivePop: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = { pop() }

    private fun <T> motion(): FiniteAnimationSpec<T> = tween(MORPH_MILLIS, easing = FastOutSlowInEasing)

    private const val FALLBACK_SCALE = 0.92f
}
