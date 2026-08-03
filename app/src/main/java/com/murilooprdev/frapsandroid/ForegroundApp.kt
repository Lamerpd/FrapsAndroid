package com.murilooprdev.frapsandroid

object ForegroundApp {

    private val focusRegex = Regex("""([a-zA-Z0-9_.]+)/[a-zA-Z0-9_.$]+""")

    fun getCurrentPackage(): String? {
        val outputs = listOf(
            RootShell.exec("dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'"),
            RootShell.exec("dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'"),
            RootShell.exec("dumpsys activity activities | grep 'mResumedActivity'")
        )

        for (output in outputs) {
            if (output.isBlank()) continue
            val match = focusRegex.find(output)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }
}
