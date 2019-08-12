package com.android.socketcan

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader


object ShellExecute {
    private const val EXIT_CODE_SUCCESS = 0
    private const val EXIT_CODE_WATCHDOG_EXIT = -1
    private const val EXIT_CODE_SHELL_DIED = -2
    private const val EXIT_CODE_SHELL_EXEC_FAILED = -3
    private const val EXIT_CODE_SHELL_WRONG_UID = -4
    private const val EXIT_CODE_SHELL_NOT_FOUND = -5
    private const val EXIT_CODE_TERMINATED = 130
    private const val EXIT_CODE_COMMAND_NOT_EXECUTABLE = 126
    private const val EXIT_CODE_COMMAND_NOT_FOUND = 127

    fun run(commands: List<String>, directory: String? = "/"): String {
        val cmds = commands.map { it.trim() }.filter { it.isNotBlank() }
        if (cmds.isEmpty()) {
            return ""
        }
        var result: String
        try {
            val process: Process = Runtime.getRuntime().exec("sh", null)
            val stdin = DataOutputStream(process.outputStream)
            val stdout = InputStreamReader(process.inputStream)
            val stderr = InputStreamReader(process.errorStream)
            directory?.let {
                stdin.write(cmdBytes("cd $it"))
                stdin.flush()
            }
            cmds.forEach {
                stdin.write(cmdBytes(it))
                stdin.flush()
            }
            stdin.write(cmdBytes("exit"));
            stdin.flush();
            // wait for our process to finish, while we gobble away in the background
            val code = process.waitFor()
            if (code == EXIT_CODE_SUCCESS) {
                result = readFully(stdout)
            } else {
                result = "${shellError(code)}, reason: ${readFully(stderr)}"
            }
            stdout.close()
            stdin.close()
            stderr.close()
            process.destroy()
        } catch (e: InterruptedException) {
            result = "watchdog exit"
        } catch (e: IOException) {
            result = "shell died with ${e.message}"
        }
        return result
    }

    @Throws(IOException::class)
    private fun readFully(input: InputStreamReader): String {
        val reader = BufferedReader(input)
        val result = StringBuffer()
        try {
            var line: String? = reader.readLine()
            while (line != null) {
                result.append(line)
                line = reader.readLine()
            }
        } catch (e: IOException) {
            // reader probably closed, expected exit condition
        }
        return result.toString()
    }

    private fun cmdBytes(cmd: String) = "$cmd\n".toByteArray(charset("UTF-8"))

    private fun shellError(error: Int): String {
        return "error code: " + when(error) {
            EXIT_CODE_WATCHDOG_EXIT -> "WATCHDOG_EXIT"
            EXIT_CODE_SHELL_DIED -> "SHELL_DIED"
            EXIT_CODE_SHELL_EXEC_FAILED -> "SHELL_EXEC_FAILED"
            EXIT_CODE_SHELL_WRONG_UID -> "SHELL_WRONG_UID"
            EXIT_CODE_SHELL_NOT_FOUND -> "SHELL_NOT_FOUND"
            EXIT_CODE_TERMINATED -> "TERMINATED"
            EXIT_CODE_COMMAND_NOT_EXECUTABLE -> "COMMAND_NOT_EXECUTABLE"
            EXIT_CODE_COMMAND_NOT_FOUND -> "COMMAND_NOT_FOUND"
            else -> "UNKNOWN($error)"
        }
    }
}
