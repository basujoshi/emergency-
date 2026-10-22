package com.bashurod.bdremote

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.graphics.Color

class MainActivity : AppCompatActivity() {
    private val projectionRequest = 4107
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 48, 36, 36)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.rgb(11, 16, 32))
        }
        val title = TextView(this).apply {
            text = "BD Mobile Remote Support"
            textSize = 24f
            setTextColor(Color.WHITE)
        }
        val info = TextView(this).apply {
            text = "This phone is the device receiving support. Sharing starts only after you approve Android's system prompt. Keep the notification visible and stop any session whenever you want."
            textSize = 15f
            setTextColor(Color.LTGRAY)
            setPadding(0, 24, 0, 24)
        }
        status = TextView(this).apply {
            text = "No active screen-sharing session."
            setTextColor(Color.rgb(74, 222, 128))
            textSize = 14f
            setPadding(0, 12, 0, 18)
        }
        val share = Button(this).apply {
            text = "Approve screen sharing"
            setOnClickListener {
                val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                startActivityForResult(mgr.createScreenCaptureIntent(), projectionRequest)
            }
        }
        val accessibility = Button(this).apply {
            text = "Review remote gesture permission"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        val stop = Button(this).apply {
            text = "Stop sharing"
            setOnClickListener {
                stopService(Intent(this@MainActivity, ScreenShareService::class.java))
                status.text = "Sharing stopped."
            }
        }
        layout.addView(title)
        layout.addView(info)
        layout.addView(status)
        layout.addView(share, match())
        layout.addView(accessibility, match())
        layout.addView(stop, match())
        setContentView(layout)
    }

    private fun match() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        topMargin = 12
    }

    @Deprecated("Uses Activity result API for simple starter clarity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != projectionRequest) return
        if (resultCode == Activity.RESULT_OK && data != null) {
            val i = Intent(this, ScreenShareService::class.java).apply {
                action = ScreenShareService.ACTION_START
                putExtra(ScreenShareService.EXTRA_RESULT_CODE, resultCode)
                putExtra(ScreenShareService.EXTRA_RESULT_DATA, data)
            }
            ContextCompat.startForegroundService(this, i)
            status.text = "Android approved screen capture. Native WebRTC transport is not wired yet."
        } else {
            status.text = "Screen sharing was declined."
        }
    }
}
