package com.sapp.mtom

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import java.io.*


class ExceptionHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
    companion object {
        private const val LINE_SEPARATOR = "\n"
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))
        val report = StringBuilder()
        report.append("************ CAUSE OF ERROR ************\n\n")
        report.append(stackTrace.toString())

        report.append("\n************ DEVICE INFORMATION ***********\n")
        report.append("Brand: ")
        report.append(Build.BRAND)
        report.append(LINE_SEPARATOR)
        report.append("Device: ")
        report.append(Build.DEVICE)
        report.append(LINE_SEPARATOR)
        report.append("Model: ")
        report.append(Build.MODEL)
        report.append(LINE_SEPARATOR)
        report.append("Id: ")
        report.append(Build.ID)
        report.append(LINE_SEPARATOR)
        report.append("Product: ")
        report.append(Build.PRODUCT)
        report.append(LINE_SEPARATOR)
        report.append("\n************ FIRMWARE ************\n")
        report.append("SDK: ")
        report.append(Build.VERSION.SDK_INT)
        report.append(LINE_SEPARATOR)
        report.append("Release: ")
        report.append(Build.VERSION.RELEASE)
        report.append(LINE_SEPARATOR)
        report.append("Incremental: ")
        report.append(Build.VERSION.INCREMENTAL)
        report.append(LINE_SEPARATOR)

        appendError(report.toString())

        val notification = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText("Ha ocurrido un error")
                .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(2, notification)

        context.stopService(Intent(context, MtoMService::class.java))
    }

    private fun appendError(text: String) {
        try {
            val file = File(Environment.getExternalStorageDirectory().absolutePath + "/GtoM",
                    "error.txt")
            if (!file.exists()) {
                if(!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                file.createNewFile()
            }

            val buf = BufferedWriter(FileWriter(file, true))
            buf.append(text)
            buf.newLine()
            buf.newLine()
            buf.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}