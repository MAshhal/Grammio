package com.mystic.grammio.presentation.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
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
 * Opens the system page where the user turns [GrammioAccessibilityService] on or off: Grammio's own
 * page where the system offers one (Android 13+), otherwise the Accessibility list with Grammio
 * highlighted, where the launcher supports that.
 */
fun Context.openGrammioAccessibilitySettings() {
    val component = grammioService().flattenToString()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val details = Intent(Settings.ACTION_ACCESSIBILITY_DETAILS_SETTINGS)
            .putExtra(Intent.EXTRA_COMPONENT_NAME, component)
        if (tryStart(details)) return
    }
    // Undocumented extras the Settings app reads to scroll to and highlight one entry.
    val list = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        .putExtra(EXTRA_FRAGMENT_ARG_KEY, component)
        .putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, Bundle().apply { putString(EXTRA_FRAGMENT_ARG_KEY, component) })
    tryStart(list)
}

private fun Context.grammioService() = ComponentName(this, GrammioAccessibilityService::class.java)

private fun Context.tryStart(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (e: ActivityNotFoundException) {
    Logger.w(e) { "No activity for ${intent.action}" }
    false
}

private const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
private const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"
