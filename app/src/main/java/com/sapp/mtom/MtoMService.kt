package com.sapp.mtom

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.sapp.mtom.BuildConfig.*
import com.sun.mail.imap.IMAPFolder
import java.util.*
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Session
import javax.mail.event.MessageCountAdapter
import javax.mail.event.MessageCountEvent


data class Email(val subject: String, val content: String)

class MtoMService : Service() {

//    private val mHandler: Handler = Handler()
//    private val mRunnable = object : Runnable {
//        override fun run() {
//            Task(this@MtoMService).execute()
//            // Repeat this the same runnable code block again.
//            mHandler.postDelayed(this, 30000)
//        }
//    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initializeService()
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
//        val thread = Thread(Runnable { mHandler.post(mRunnable) })
        val thread = Thread(Runnable {
            readGMail()
        })
        thread.start()

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
//        mHandler.removeCallbacks(mRunnable)
        // Remove notification
        stopForeground(true)
    }

    private fun restartService() {
        val intent = Intent(this, MtoMService::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, intent, 0)
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(AlarmManager.RTC_WAKEUP,
                Date().time + 1000, pendingIntent)
    }

//    companion object {
//        @SuppressLint("StaticFieldLeak")
//        private class Task(private val context: Context) : AsyncTask<Void, Void, Void>() {
//
//            override fun doInBackground(vararg params: Void): Void? {
//                readGMail()
//                return null
//            }
//        }
//    }

    private fun readGMail() {
//        try {
        // Get a Properties object
        val props = System.getProperties()
        props.setProperty("mail.imap.ssl.enable", PROP_SSL_ENABLED)
        // Get a Session object
        val session = Session.getInstance(props)
        // Get a Store object
        val store = session.getStore("imap")
        // Connect
        store.connect(HOST, PORT, USER, PASS)

        // Get INBOX Folder
        val folder = store.getFolder("INBOX")
        // Try to open write
        folder.open(Folder.READ_WRITE)

        // First batch of messages
        var msgnum = 1
        var count = folder.messageCount
        while (msgnum <= count) {
            while (msgnum <= count) {
                // Get message and send
                val msg = folder.getMessage(msgnum)
                if (msg.isMimeType("text/plain")) {
                    sendMsg(Email(msg.subject, msg.content.toString()))
                }
                msg.setFlag(Flags.Flag.DELETED, true)

                msgnum++
            }
            count = folder.messageCount
        }
        // Processed all

        // Expunge (permanently remove) messages marked DELETED
//        folder.expunge()

        // Add MessageCountListener to listen for new messages
        folder.addMessageCountListener(object : MessageCountAdapter() {
            override fun messagesAdded(ev: MessageCountEvent) {
                // Get message and send
                for (msg in ev.messages) {
                    if (msg.isMimeType("text/plain")) {
                        sendMsg(Email(msg.subject, msg.content.toString()))
                    }
                    msg.setFlag(Flags.Flag.DELETED, true)
                }
                // Expunge (permanently remove) messages marked DELETED
//                folder.expunge()
            }
        })

        // Wait for new messages
        while (true)
            (folder as IMAPFolder).idle()

//        } catch (e: Exception) {
//            ExceptionHandler(this).uncaughtException(Thread.currentThread(), e)
//        }
    }

    private val smsManager = SmsManager.getDefault()
    private fun sendMsg(email: Email) {
        when (FLAVOR) {
            "gmail" -> {
                smsManager.sendMultipartTextMessage(email.subject, null,
                        smsManager.divideMessage(email.content), null,
                        null)
            }
            "nauta" -> {
                val settings = Settings()
                settings.useSystemSending = true
                val transaction = Transaction(this, settings)
                val message = Message(email.content, email.subject)
                message.sendAsMMS(true)
                transaction.sendNewMessage(message, Transaction.NO_THREAD_ID)
            }
        }
    }

}
