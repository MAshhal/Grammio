package com.mystic.grammio.presentation.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import co.touchlab.kermit.Logger

/** Whether the user has turned on [GrammioAccessibilityService] in the system's Accessibility settings. */
fun Context.isGrammioAccessibilityEnabled(): Boolean {
    val grammio = grammioService()
    return getSystemService(AccessibilityManager::class.java)
        .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .map { it.resolveInfo.serviceInfo }
        .any { ComponentName(it.packageName, it.name) == grammio }
}

/**
 * Opens the system's Accessibility settings, where the user turns [GrammioAccessibilityService] on
 * or off, scrolled to Grammio and highlighted where the Settings app supports that. Grammio's own
 * page (ACCESSIBILITY_DETAILS_SETTINGS) needs a permission only system apps get.
 */
fun Context.openGrammioAccessibilitySettings() {
    val component = grammioService().flattenToString()
    // Undocumented extras the Settings app reads to scroll to and highlight one entry.
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        .putExtra(EXTRA_FRAGMENT_ARG_KEY, component)
        .putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, Bundle().apply { putString(EXTRA_FRAGMENT_ARG_KEY, component) })
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Logger.w(e) { "No Accessibility settings to open" }
    }
}

private fun Context.grammioService() = ComponentName(this, GrammioAccessibilityService::class.java)

private const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
private const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"
