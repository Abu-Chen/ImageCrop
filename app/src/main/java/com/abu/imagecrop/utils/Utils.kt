package com.abu.imagecrop.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

enum class Permssion {
    GRANTED,
    DENIED
}

class Utils {
    companion object {
        fun checkPermission(context: Context, vararg permssions: String): Permssion {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return Permssion.GRANTED
            }
            for(permission in permssions) {
                if(context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    return Permssion.DENIED
                }
            }
            return Permssion.GRANTED
        }

        fun getCacheCameraUri(context: Context) : Uri{
            return Uri.fromFile(File(context.externalCacheDir!!.absolutePath + "/TempCamera"))
        }

        fun getCacheUri(context: Context): Uri {
            return File(context.externalCacheDir!!.absolutePath + "/TempCamera").apply {
                this.createNewFile()
            }.let {
                FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", it)
            }
        }
    }
}