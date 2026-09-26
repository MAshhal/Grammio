package com.mystic.grammio.presentation.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

/**
 * Horizontal slides for the Settings back stack. The page on top moves the full width while the
 * one underneath moves a quarter of it, for depth. Nothing fades: a half-transparent page lets the
 * window show through, which is what flashed white.
 */
internal object SettingsTransitions {

    /** Opening a page: it slides in over the current one. */
    val push: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        slideIntoContainer(SlideDirection.Start, motion()) togetherWith
            slideOutOfContainer(SlideDirection.Start, motion()) { it / PARALLAX_DIVISOR }
    }

    /** Back button or back arrow: the page slides away and uncovers the previous one. */
    val pop: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        slideIntoContainer(SlideDirection.End, motion()) { it / PARALLAX_DIVISOR } togetherWith
            slideOutOfContainer(SlideDirection.End, motion())
    }

    /**
     * Back gesture: the page follows the finger away from the edge the swipe started at, shrinking a
     * little like a card being pulled off, and slides out when the gesture completes.
     */
    val predictivePop: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = { swipeEdge ->
        // Physical directions: the gesture comes from a physical edge, whatever the layout direction.
        val towards = if (swipeEdge == NavigationEvent.EDGE_RIGHT) SlideDirection.Left else SlideDirection.Right
        slideIntoContainer(towards, motion()) { it / PARALLAX_DIVISOR } togetherWith
            (slideOutOfContainer(towards, motion()) + scaleOut(motion(), targetScale = PREDICTIVE_SCALE))
    }

    private fun <T> motion(): FiniteAnimationSpec<T> = tween(DURATION_MILLIS, easing = FastOutSlowInEasing)

    private const val DURATION_MILLIS = 400
    private const val PARALLAX_DIVISOR = 4
    private const val PREDICTIVE_SCALE = 0.9f
}
