package com.dragon.tribe.fire.oc.maker.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

object PermissionHelper {
    val storagePermission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> emptyArray()

        else -> arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    fun checkPermissions(permissions: Array<String>, activity: Activity): Boolean {
        return permissions.all {
            ActivityCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    val cameraPermission = arrayOf(Manifest.permission.CAMERA)
}