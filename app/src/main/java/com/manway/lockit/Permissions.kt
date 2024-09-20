package com.manway.lockit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.manway.lockit.Handler.PackageHandler



@SuppressLint("ObsoleteSdkInt")
fun Activity.checkOverlayPermission(): Boolean {
    return Settings.canDrawOverlays(this)
}

fun Activity.checkUsageAccessPermission(): Boolean {
    return PackageHandler.getCurrentApk(this).isNotEmpty()
}

fun Activity.checkAccessibilityServicePermission(): Boolean {
    val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    return enabledServices != null && enabledServices.contains(packageName)
}

@SuppressLint("ObsoleteSdkInt")
fun Activity.requestOverlayPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.setData(Uri.parse("package:" + packageName))
        startActivityForResult(intent, 100)
    }
}

fun Activity.requestUsageAccessPermission() {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    startActivityForResult(intent, 101)
}

fun Activity.requestAccessibilityServicePermission() {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    startActivityForResult(intent, 102)
}

fun Activity.requestAccessibilityService() {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    startActivityForResult(intent, 102)
}