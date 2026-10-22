package com.bashurod.bdremote

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

/**
 * Receives normalized tap coordinates only from a verified, active support session.
 * IMPORTANT: wire this method to authenticated server messages only after implementing
 * per-session authorization and a visible consent/active-session state.
 */
class RemoteControlAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    fun dispatchRemoteTap(normalizedX: Float, normalizedY: Float): Boolean {
        if (normalizedX !in 0f..1f || normalizedY !in 0f..1f) return false
        val metrics = resources.displayMetrics
        val x = normalizedX * metrics.widthPixels
        val y = normalizedY * metrics.heightPixels
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 80)
        return dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }
}
