package com.github.sapp.gtom

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS),
                    1)
        }

        var running = isServiceRunning()
        if (running) {
            tvStatus.text = getText(R.string.service_running)
        } else {
            tvStatus.text = getText(R.string.service_stop)
        }

        fab.setOnClickListener { _ ->
            val intent = Intent(this, GtoMService::class.java)
            if (!running) {
                startService(intent)
                tvStatus.text = getText(R.string.service_running)
            } else {
                stopService(intent)
                tvStatus.text = getText(R.string.service_stop)
            }
            running = !running
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
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
