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
            text = if (RootShell.isRootAvailable()) "Root: OK" else "Root: NAO disponivel"
        }

        val autoLabel = TextView(this).apply {
            text = "Modo automatico: segue qualquer app/tela em foco. " +
                "So preenche o campo abaixo se quiser travar num pacote fixo."
            setPadding(0, 24, 0, 8)
        }

        packageInput = EditText(this).apply {
            hint = "opcional: pacote fixo (ex: com.mojang.minecraftpe)"
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
        root.addView(autoLabel)
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
            Toast.makeText(this, "Permissao ja concedida", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Concede a permissao de overlay primeiro", Toast.LENGTH_SHORT).show()
            return
        }
        val target = packageInput.text.toString().trim()
        val intent = Intent(this, OverlayService::class.java).apply {
            if (target.isNotEmpty()) {
                putExtra(OverlayService.EXTRA_TARGET_PACKAGE, target)
            }
        }
        startForegroundService(intent)
        Toast.makeText(
            this,
            if (target.isEmpty()) "Modo automatico ativado" else "Travado em: $target",
            Toast.LENGTH_SHORT
        ).show()
    }
}
