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

    fun dumpFps(packageName: String, windowSeconds: Double): Int {
        if (windowSeconds <= 0) return 0

        val output = RootShell.exec("dumpsys SurfaceFlinger --timestats -dump")
        if (output.isBlank()) return 0

        val blocks = output.split(Regex("(?=layerName\\s*=)"))
        val block = blocks.firstOrNull { it.contains(packageName) } ?: return 0

        val match = Regex("""totalFrames\s*=\s*(\d+)""").find(block) ?: return 0
        val frames = match.groupValues[1].toIntOrNull() ?: return 0

        return (frames / windowSeconds).toInt()
    }

    fun disable() {
        RootShell.exec("dumpsys SurfaceFlinger --timestats -disable")
        enabled = false
    }
}
