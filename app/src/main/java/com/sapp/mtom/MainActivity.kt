package com.sapp.mtom

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
            val intent = Intent(this, MtoMService::class.java)
            if (!isServiceRunning()) {
                setComponentEnabledSetting(COMPONENT_ENABLED_STATE_ENABLED)
                startService(intent)
            } else {
                stopService(intent)
                setComponentEnabledSetting(COMPONENT_ENABLED_STATE_DISABLED)
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
            if (serviceInfo.service.className == MtoMService::class.java.name &&
                    serviceInfo.pid != 0) {
                return true
            }
        }

        return false
    }

    private fun setComponentEnabledSetting(COMPONENT_ENABLED_STATE: Int) {
        packageManager.setComponentEnabledSetting(ComponentName(this, MtoMService::class.java),
                COMPONENT_ENABLED_STATE, DONT_KILL_APP)
    }
}
