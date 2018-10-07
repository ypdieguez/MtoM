package com.sapp.mtom

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class Permissions {
    companion object {
        private val PERMISSIONS = arrayOf(SEND_SMS, WRITE_EXTERNAL_STORAGE)
        fun check(context: Context) : Boolean {
            for (permission in PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(context, permission) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }

        fun request(activity: MainActivity) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, 1)
        }
    }
}

