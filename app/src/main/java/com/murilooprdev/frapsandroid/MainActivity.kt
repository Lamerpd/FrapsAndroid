package com.murilooprdev.frapsandroid

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var packageInput: EditText
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
        }

        statusText = TextView(this).apply {
            text = if (RootShell.isRootAvailable()) "Root: OK" else "Root: NÃO disponível"
        }

        packageInput = EditText(this).apply {
            hint = "pacote do app alvo (ex: com.mojang.minecraftpe)"
        }

        val permissionButton = Button(this).apply {
            text = "1. Permitir overlay"
            setOnClickListener { requestOverlayPermission() }
        }

        val startButton = Button(this).apply {
            text = "2. Iniciar overlay de FPS"
            setOnClickListener { startOverlay() }
        }

        val stopButton = Button(this).apply {
            text = "Parar overlay"
            setOnClickListener { stopService(Intent(this@MainActivity, OverlayService::class.java)) }
        }

        root.addView(statusText)
        root.addView(packageInput)
        root.addView(permissionButton)
        root.addView(startButton)
        root.addView(stopButton)
        setContentView(root)
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            Toast.makeText(this, "Permissão já concedida", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Concede a permissão de overlay primeiro", Toast.LENGTH_SHORT).show()
            return
        }
        val target = packageInput.text.toString().trim()
        if (target.isEmpty()) {
            Toast.makeText(this, "Digita o pacote do app alvo", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra(OverlayService.EXTRA_TARGET_PACKAGE, target)
        }
        startForegroundService(intent)
    }
}
