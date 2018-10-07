package com.github.sapp.gtom

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.telephony.SmsManager
import java.lang.*
import java.util.*
import javax.mail.Folder
import javax.mail.MessagingException
import javax.mail.Session


data class Email(val subject: String, val content: String)

class GtoMService : Service() {

    private var initialized: Boolean = false
    private val mHandler: Handler = Handler()
    private val mRunnable = object : Runnable {
        override fun run() {
            Task().execute(this@GtoMService)
            // Repeat this the same runnable code block again.
            mHandler.postDelayed(this, 15000)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!initialized) {
            initializeService()
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        finishService()
        restartService()
    }

    private fun initializeService() {
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))

        // Start task
        val thread = Thread(Runnable { mHandler.post(mRunnable) })
        thread.start()

        initialized = true

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.service_running))
                .setContentIntent(pendingIntent).build()

        startForeground(1, notification)
    }

    private fun finishService() {
        initialized = false
        mHandler.removeCallbacks(mRunnable)
        // Remove notification
        stopForeground(true)
    }

    private fun restartService() {
        val intent = Intent(this, GtoMService::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, intent, 0)
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(AlarmManager.RTC_WAKEUP,
                Date().time + 1000, pendingIntent)
    }

    companion object {
        private class Task : AsyncTask<Context, Void, Void>() {

            override fun doInBackground(vararg params: Context): Void? {
                readGMail(params[0])
                return null
            }

            private fun readGMail(context: Context) {
                try {
                    // Get a Properties object
                    val props = System.getProperties()
                    props.setProperty("mail.pop3.ssl.enable", "true")
                    // Get a Session object
                    val session = Session.getInstance(props)
                    // Get a Store object
                    val store = session.getStore("pop3")
                    // Connect
                    store.connect("pop.gmail.com", 995, "gtom20180828@gmail.com",
                            "GtoM_2018/08/28")

                    // Open the Folder
                    val folder = store.getFolder("INBOX")

                    // try to open read/write and if that fails try read-only
                    try {
                        folder.open(Folder.READ_WRITE)
                    } catch (ex: MessagingException) {
                        folder.open(Folder.READ_ONLY)
                    }

                    // Get messages and send
                    for (msg in folder.messages) {
                        if (msg.isMimeType("text/plain")) {
                            sendMsg(Email(msg.subject, msg.content.toString()))
                        }
                    }

                    //Close
                    folder.close()
                    store.close()
                } catch (e: Exception) {
                    ExceptionHandler(context).uncaughtException(Thread.currentThread(), e)
                }
            }

            private fun sendMsg(email: Email) {
                val smsManager = SmsManager.getDefault()
                smsManager.sendMultipartTextMessage(email.subject, null,
                        smsManager.divideMessage(email.content), null, null)
            }
        }
    }
}
