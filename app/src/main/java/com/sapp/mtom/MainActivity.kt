package com.sapp.mtom

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.github.sapp.gtom.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (!Permissions.check(this)) {
            Permissions.request(this)
        }

        updateUI(isServiceRunning())

        fab.setOnClickListener { _ ->
            val intent = Intent(this, GtoMService::class.java)
            if (!isServiceRunning()) {
                startService(intent)
            } else {
                stopService(intent)
            }
            updateUI(isServiceRunning())
        }
    }

    private fun updateUI(running: Boolean) {
        if (running) {
            tvStatus.text = getText(R.string.service_running)
            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.stop))
        } else {
            tvStatus.text = getText(R.string.service_stop)
            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.play))
        }
    }

    /**
     * If service is running
     *
     * @return boolean
     */
    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val services = manager.getRunningServices(Integer.MAX_VALUE)
        for (serviceInfo in services) {
            if (serviceInfo.service.className == GtoMService::class.java.name &&
                    serviceInfo.pid != 0) {
                return true
            }
        }

        return false
    }
}
