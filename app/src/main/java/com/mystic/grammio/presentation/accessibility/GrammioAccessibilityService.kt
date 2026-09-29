package com.mystic.grammio.presentation.accessibility

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import co.touchlab.kermit.Logger
import com.mystic.grammio.R
import com.mystic.grammio.presentation.process.ProcessTextActivity
import org.koin.android.ext.android.inject

/**
 * Offers Grammio through the accessibility button or shortcut, for apps whose text-selection menu
 * doesn't list PROCESS_TEXT actions. Pressing it opens the same sheet on whatever is selected, and
 * Replace writes the result back into the field through [SelectionReplacer].
 *
 * Kept thin like an Activity: it finds the selection and opens the sheet. It never logs or keeps
 * the text of other apps, only a reference to the view where text was last selected.
 */
class GrammioAccessibilityService : AccessibilityService() {

    private val selectionReplacer: SelectionReplacer by inject()

    /** Where text was last selected; the focused field may not be it (read-only text has no focus). */
    private var lastSelectionSource: AccessibilityNodeInfo? = null

    private val buttonCallback = object : AccessibilityButtonController.AccessibilityButtonCallback() {
        override fun onClicked(controller: AccessibilityButtonController) = openSheet()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        accessibilityButtonController.registerAccessibilityButtonCallback(buttonCallback)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            event.source?.let { lastSelectionSource = it }
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        accessibilityButtonController.unregisterAccessibilityButtonCallback(buttonCallback)
        selectionReplacer.clear()
        lastSelectionSource = null
        super.onDestroy()
    }

    private fun openSheet() {
        val found = findSelection()
        if (found == null) {
            Toast.makeText(this, R.string.accessibility_no_selection, Toast.LENGTH_SHORT).show()
            return
        }
        val (node, selection) = found
        selectionReplacer.hold(NodeEditableField(node), selection)
        startActivity(
            ProcessTextActivity.accessibilityIntent(this, selection.selectedText, readOnly = !selection.editable),
        )
    }

    private fun findSelection(): Pair<AccessibilityNodeInfo, TextSelection>? {
        val focused = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        return listOfNotNull(focused, lastSelectionSource)
            .firstNotNullOfOrNull { node -> node.selection()?.let { node to it } }
    }

    /** The node's current selection; refreshed first, since it may have changed since we got it. */
    private fun AccessibilityNodeInfo.selection(): TextSelection? {
        if (!refresh() || isPassword) return null
        return TextSelection.of(text, textSelectionStart, textSelectionEnd, editable = isEditable)
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
