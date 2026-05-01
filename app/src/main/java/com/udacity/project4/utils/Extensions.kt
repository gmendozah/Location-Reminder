package com.udacity.project4.utils

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter

/**
 * Extension function to setup the RecyclerView.
 */
fun <T> RecyclerView.setup(
    adapter: BaseRecyclerViewAdapter<T>
) {
    this.apply {
        layoutManager = LinearLayoutManager(this.context)
        this.adapter = adapter
    }
}

fun Fragment.setTitle(title: String) {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).supportActionBar?.title = title
    }
}

fun Fragment.setDisplayHomeAsUpEnabled(bool: Boolean) {
    if (activity is AppCompatActivity) {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(bool)
    }
}

fun View.fadeIn() {
    this.visibility = View.VISIBLE
    this.alpha = 0f
    this.animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeIn.alpha = 1f
        }
    })
}

fun View.fadeOut() {
    this.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeOut.alpha = 0f
            this@fadeOut.visibility = View.GONE
        }
    })
}

/**
 * Permission Management
 */
const val REQUEST_LOCATION_PERMISSION = 33

/**
 * Checks if both foreground and background location permissions are granted,
 * and also checks for notification permissions on Android 13+.
 */
fun Context.locationPermissionsApproved(): Boolean {
    val foregroundApproved = (PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))
    val backgroundApproved =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            true
        }
    val notificationsApproved =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
    return foregroundApproved && backgroundApproved && notificationsApproved
}

/**
 * Returns the permissions that still need to be requested.
 * Handles the tiered request logic (Foreground first, then Background, then Notifications).
 */
fun Context.getPermissionsToRequest(): Array<String> {
    val permissions = mutableListOf<String>()

    // 1. First priority: Notifications (Android 13+) - Simple dialog, request first
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationsApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!notificationsApproved) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            return permissions.toTypedArray()
        }
    }

    // 2. Second priority: Foreground Location
    val foregroundApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!foregroundApproved) {
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        return permissions.toTypedArray()
    }

    // 3. Third priority: Background Location (Android 10+) - Requires Settings, request last
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val backgroundApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!backgroundApproved) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            return permissions.toTypedArray()
        }
    }

    return permissions.toTypedArray()
}

fun Activity.showPermissionDeniedDialog() {
    if (locationPermissionsApproved()) return
    val notificationsApproved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    val foregroundApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val backgroundApproved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val message = when {
        !notificationsApproved -> R.string.notification_permission_denied_explanation
        !foregroundApproved -> R.string.location_permission_denied_explanation
        !backgroundApproved -> R.string.background_location_permission_denied_explanation
        else -> R.string.permissions_denied_explanation
    }

    androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle(R.string.location_required_error)
        .setMessage(message)
        .setPositiveButton(R.string.settings) { _, _ ->
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
        .setNegativeButton(android.R.string.cancel, null)
        .create()
        .show()
}

fun Fragment.showPermissionDeniedDialog() {
    requireActivity().showPermissionDeniedDialog()
}
