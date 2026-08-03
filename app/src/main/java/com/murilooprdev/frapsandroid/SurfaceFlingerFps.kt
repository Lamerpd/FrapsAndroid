package com.murilooprdev.frapsandroid

/**
 * Lê os timestamps reais de frame do SurfaceFlinger via root.
 * Formato de "dumpsys SurfaceFlinger --latency <window>":
 *   linha 1: período de refresh em nanosegundos
 *   linhas seguintes: desired | actual | ready (nanosegundos), separadas por tab
 *   uma linha com os 3 valores zerados marca "sem frame nesse slot"
 */
object SurfaceFlingerFps {

    /** Acha o nome exato da janela/surface pro pacote informado. */
    fun findWindowName(packageName: String): String? {
        val list = RootShell.exec("dumpsys SurfaceFlinger --list")
        return list.lineSequence().firstOrNull { it.contains(packageName) }?.trim()
    }

    /** Retorna a lista de timestamps "actual present time" (coluna 2) não-zero, em nanosegundos. */
    private fun readActualPresentTimes(windowName: String): List<Long> {
        val raw = RootShell.exec("dumpsys SurfaceFlinger --latency \"$windowName\"")
        val lines = raw.lineSequence().drop(1) // primeira linha = refresh period
        val timestamps = mutableListOf<Long>()
        for (line in lines) {
            val parts = line.trim().split("\\s+".toRegex())
            if (parts.size < 3) continue
            val actual = parts[1].toLongOrNull() ?: continue
            if (actual != 0L && actual != Long.MAX_VALUE) {
                timestamps.add(actual)
            }
        }
        return timestamps.sorted()
    }

    /**
     * Calcula o FPS instantâneo comparando duas leituras de timestamps
     * separadas no tempo (chame isso a cada ~500ms-1s a partir de uma
     * coroutine/loop no Service).
     */
    fun computeFps(windowName: String, windowMillis: Long = 1000): Double {
        val timestamps = readActualPresentTimes(windowName)
        if (timestamps.size < 2) return 0.0

        val nowNanos = timestamps.last()
        val cutoff = nowNanos - (windowMillis * 1_000_000)
        val recent = timestamps.filter { it >= cutoff }
        if (recent.size < 2) return 0.0

        val elapsedNanos = recent.last() - recent.first()
        if (elapsedNanos <= 0) return 0.0

        val frameCount = recent.size - 1
        return frameCount * 1_000_000_000.0 / elapsedNanos
    }
}
