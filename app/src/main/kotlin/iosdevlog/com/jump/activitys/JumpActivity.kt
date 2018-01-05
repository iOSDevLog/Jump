package iosdevlog.com.jump.activitys

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.accessibility.AccessibilityManager
import android.widget.SeekBar
import iosdevlog.com.jump.R
import iosdevlog.com.jump.utils.Utils
import kotlinx.android.synthetic.main.activity_jump.*
import kotlinx.android.synthetic.main.fragment_jump.*

/**
 * Created iosdevlog on 2018/1/5.
 */

class JumpActivity : AppCompatActivity() {
    val REQUEST_WRITE_EXTNARL_PERMISSION = 100

    private val mAccessibleIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jump)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            startActivity(mAccessibleIntent)
        }

        seekBar.setOnSeekBarChangeListener(SeekBarListener())
    }


    internal class SeekBarListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            Utils.resizedDistancePressTimeRatio = progress / 100.0
        }


        override fun onStartTrackingTouch(seekBar: SeekBar) {
            Utils.screencap()
        }


        override fun onStopTrackingTouch(seekBar: SeekBar) {
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_jump, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val hasWriteSdcardPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteSdcardPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf("REQUEST_WRITE_EXTNARL_PERMISSION"),
                        REQUEST_WRITE_EXTNARL_PERMISSION
                )
                return
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_WRITE_EXTNARL_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()

        updateServiceStatus()
    }

    private fun updateServiceStatus() {
        var serviceEnabled = false

        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val accessibilityServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (info in accessibilityServices) {
            if (info.id == packageName + "/.JumpAccessibilityService") {
                serviceEnabled = true
            }
        }

        if (serviceEnabled) {
            service_textview.setText(R.string.services_on)
            fab.setImageResource(R.drawable.ic_stop_black_24dp)
        } else {
            service_textview.setText(R.string.services_off)
            fab.setImageResource(R.drawable.ic_play_arrow_black_24dp)
        }
    }
}
