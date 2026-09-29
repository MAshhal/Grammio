package com.mystic.grammio.presentation.accessibility

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import com.mystic.grammio.R

/**
 * Grammio's button, floated just above the keyboard's top-right corner while the user types. An
 * accessibility overlay: it needs no extra permission and never takes focus from the field.
 */
class KeyboardButton(
    context: Context,
    onPress: () -> Unit,
) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val size = context.resources.getDimensionPixelSize(R.dimen.keyboard_button_size)
    private val margin = context.resources.getDimensionPixelSize(R.dimen.keyboard_button_margin)

    private val view = ImageView(context).apply {
        setImageResource(R.mipmap.ic_launcher_round)
        contentDescription = context.getString(R.string.accessibility_button_description)
        setOnClickListener { onPress() }
    }

    private val params = WindowManager.LayoutParams(
        size,
        size,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.END
        x = margin
    }

    private var shown = false

    /** Shows the button, or moves it, so it sits above a keyboard whose top edge is at [keyboardTop]. */
    fun showAbove(keyboardTop: Int) {
        val y = (keyboardTop - size - margin).coerceAtLeast(0)
        if (shown && params.y == y) return
        params.y = y
        if (shown) windowManager.updateViewLayout(view, params) else windowManager.addView(view, params)
        shown = true
    }

    fun hide() {
        if (!shown) return
        windowManager.removeView(view)
        shown = false
    }
}
