package com.murilooprdev.frapsandroid

object SurfaceFlingerFps {

    @Volatile private var enabled = false

    private fun ensureEnabled() {
        if (!enabled) {
            RootShell.exec("dumpsys SurfaceFlinger --timestats -enable")
            enabled = true
        }
    }

    fun clear() {
        ensureEnabled()
        RootShell.exec("dumpsys SurfaceFlinger --timestats -clear")
    }

    fun dumpFps(packageName: String): Int {
        val output = RootShell.exec("dumpsys SurfaceFlinger --timestats -dump")
        if (output.isBlank()) return 0

        val blocks = output.split(Regex("(?=layerName\\s*=)"))
        val matches = blocks.filter { it.contains(packageName) }
        if (matches.isEmpty()) return 0

        val fpsValues = matches.mapNotNull { block ->
            Regex("""averageFPS\s*=\s*([0-9]+\.?[0-9]*)""")
                .find(block)?.groupValues?.get(1)?.toDoubleOrNull()
        }

        return fpsValues.maxOrNull()?.toInt() ?: 0
    }

    fun disable() {
        RootShell.exec("dumpsys SurfaceFlinger --timestats -disable")
        enabled = false
    }
}
