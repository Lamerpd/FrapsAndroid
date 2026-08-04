package com.murilooprdev.frapsandroid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import kotlin.concurrent.thread

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var fpsView: FpsCounterView
    private val mainHandler = Handler(Looper.getMainLooper())

    private var lastForegroundPackage: String? = null
    @Volatile private var running = false

    companion object {
        const val EXTRA_TARGET_PACKAGE = "target_package"
        private const val CHANNEL_ID = "fraps_overlay_channel"
        private const val NOTIF_ID = 1
        private const val POLL_INTERVAL_MS = 1000L
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        fpsView = FpsCounterView(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())

        val fixedPackage = intent?.getStringExtra(EXTRA_TARGET_PACKAGE)
        if (!fixedPackage.isNullOrBlank()) {
            lastForegroundPackage = fixedPackage
        }

        addOverlayView()
        startFpsLoop(followForeground = fixedPackage.isNullOrBlank())
        return START_STICKY
    }

    private fun addOverlayView() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 16
            y = 16
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        windowManager.addView(fpsView, params)
    }

    private fun startFpsLoop(followForeground: Boolean) {
        running = true
        thread(name = "fps-poll") {
            SurfaceFlingerFps.clear()

            while (running) {
                Thread.sleep(POLL_INTERVAL_MS)

                if (followForeground) {
                    val currentPackage = ForegroundApp.getCurrentPackage()
                    if (currentPackage != null && currentPackage != packageName) {
                        lastForegroundPackage = currentPackage
                    }
                }

                val pkg = lastForegroundPackage
                val fps = if (pkg != null) SurfaceFlingerFps.dumpFps(pkg) else 0

                mainHandler.post { fpsView.fps = fps }

                SurfaceFlingerFps.clear()
            }

            SurfaceFlingerFps.disable()
        }
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Fraps Overlay", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Fraps Android")
            .setContentText("Contador de FPS ativo")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
    }

    override fun onDestroy() {
        running = false
        if (::fpsView.isInitialized) {
            windowManager.removeView(fpsView)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
