package com.github.sapp.gtom

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.PendingIntent
import android.os.Handler
import android.support.v4.app.NotificationCompat
import android.telephony.SmsManager
import com.sun.mail.util.MailConnectException
import java.lang.Exception
import javax.mail.AuthenticationFailedException
import javax.mail.Folder
import javax.mail.MessagingException
import javax.mail.Session
import android.os.AsyncTask



data class Email(val subjet: String, val content: String)

class GtoMService : Service() {

    private val mHandler: Handler = Handler()
    private val mRunnable = object : Runnable {
        override fun run() {
            Task().execute()
            // Repeat this the same runnable code block again.
            mHandler.postDelayed(this, 60000)
        }
    }

    private inner class Task : AsyncTask<String, Int, Void>() {
        override fun doInBackground(vararg params: String?): Void? {
            for (email in readGMail()) {
                senMsg(email)
            }
            return null
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GtoM")
                .setContentText("Corriendo servicio...")
                .setContentIntent(pendingIntent).build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start task

        val thread = Thread(Runnable { mHandler.post(mRunnable) })
        thread.priority = Thread.MAX_PRIORITY
        thread.start()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop Service
        mHandler.removeCallbacks(mRunnable)
    }

    private fun readGMail(): MutableList<Email> {

        val emails: MutableList<Email> = mutableListOf()

        val username = "gtom20180828@gmail.com"
        val password = "GtoM_2018/08/28"

        // Get a Properties object
        val props = System.getProperties()

        // Start SSL connection
        props["mail.pop3.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"

        // Get a Session object
        val session = Session.getInstance(props)
        // Get a Store object
        val store = session.getStore("pop3")

        try {
            // Connect
            store.connect("pop.gmail.com", 995, username, password)

            // Open the Folder
//            var folder = store.defaultFolder
//            folder = folder.getFolder("INBOX")

            val folder = store.getFolder("INBOX")

            // try to open read/write and if that fails try read-only
            try {
                folder.open(Folder.READ_WRITE)
            } catch (ex: MessagingException) {
                folder.open(Folder.READ_ONLY)
            }

            // Get messages
            for (msg in folder.messages) {
                if (msg.isMimeType("text/plain")) {
                    emails.add(Email(msg.subject, msg.content.toString()))
                }
            }

            //Close
            folder.close()


        } catch (e: MailConnectException) {
            val a = 1
            // TODO: Do action to alert.
        } catch (e: AuthenticationFailedException) {
            val a = 1
        } catch (e: Exception) {
            val a = 1
        } finally {
           store.close()
        }

        // Return emails
        return emails
    }

    private fun senMsg(email: Email) {
        SmsManager.getDefault().sendTextMessage(email.subjet, null,
                email.content, null, null)
    }
}
