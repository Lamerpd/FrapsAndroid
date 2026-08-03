package com.murilooprdev.frapsandroid

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Executa comandos via `su`. Assume que o device já tem root
 * (Magisk/KernelSU) e que o usuário concede o pedido de permissão
 * na primeira chamada.
 */
object RootShell {

    fun isRootAvailable(): Boolean {
        return try {
            val process = ProcessBuilder("su", "-c", "id").start()
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }

    /** Executa um comando via su e retorna a saída completa (stdout). */
    fun exec(command: String): String {
        return try {
            val process = ProcessBuilder("su", "-c", command).start()
            val output = BufferedReader(InputStreamReader(process.inputStream))
                .readText()
            process.waitFor()
            output
        } catch (e: Exception) {
            ""
        }
    }
}
