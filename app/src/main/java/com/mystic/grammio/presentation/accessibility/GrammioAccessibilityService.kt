package com.mystic.grammio.presentation.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.Toast
import co.touchlab.kermit.Logger
import com.mystic.grammio.R
import com.mystic.grammio.presentation.process.ProcessTextActivity
import org.koin.android.ext.android.inject

/**
 * Offers Grammio in apps whose text-selection menu doesn't list PROCESS_TEXT actions. While the
 * keyboard is up for a text field, a [KeyboardButton] floats above it; pressing it opens the same
 * sheet on the selected text, or on all of the field's text when nothing is selected. Replace writes
 * the result back into the field through [SelectionReplacer].
 *
 * Kept thin like an Activity: it decides when to show the button and opens the sheet. It reads a
 * field's text only when the button is pressed, and never logs or keeps it.
 */
class GrammioAccessibilityService : AccessibilityService() {

    private val selectionReplacer: SelectionReplacer by inject()

    private var button: KeyboardButton? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        button = KeyboardButton(this, onPress = ::openSheet)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) = updateButton()

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        button?.hide()
        button = null
        selectionReplacer.clear()
        super.onDestroy()
    }

    private fun updateButton() {
        val button = button ?: return
        val keyboardTop = keyboardTop()
        val field = findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?.let { TypingField(it.packageName?.toString(), it.isEditable, it.isPassword) }
        if (keyboardTop != null && field?.offersButton(ownPackage = packageName) == true) {
            button.showAbove(keyboardTop)
        } else {
            button.hide()
        }
    }

    /** Top edge of the keyboard on screen, or null when no keyboard is showing. */
    private fun keyboardTop(): Int? = windows
        .firstOrNull { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
        ?.let { keyboard -> Rect().also(keyboard::getBoundsInScreen).top }

    private fun openSheet() {
        val node = findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val selection = node?.selectionOrAll()
        if (selection == null) {
            Toast.makeText(this, R.string.accessibility_no_text, Toast.LENGTH_SHORT).show()
            return
        }
        selectionReplacer.hold(NodeEditableField(node), selection)
        // The sheet takes focus, so the keyboard and the button go away until the user types again.
        button?.hide()
        startActivity(
            ProcessTextActivity.accessibilityIntent(this, selection.selectedText, readOnly = !selection.editable),
        )
    }

    /** The field's selection, or all its text; refreshed first, since it may have changed since. */
    private fun AccessibilityNodeInfo.selectionOrAll(): TextSelection? {
        if (!refresh() || isPassword) return null
        // An empty field reports its hint as its text.
        val text = text.takeUnless { isShowingHintText }
        return TextSelection.selectionOrAll(text, textSelectionStart, textSelectionEnd, editable = isEditable)
    }

    private class NodeEditableField(private val node: AccessibilityNodeInfo) : EditableField {
        override fun setText(
            text: String,
            cursor: Int,
        ): Boolean {
            if (!node.refresh()) return false
            val setText = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            if (!node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, setText)) {
                Logger.w { "The app refused the replaced text" }
                return false
            }
            val setSelection = Bundle().apply {
                putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, cursor)
                putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, cursor)
            }
            // Only cosmetic, so its result doesn't matter.
            node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, setSelection)
            return true
        }
    }
}
