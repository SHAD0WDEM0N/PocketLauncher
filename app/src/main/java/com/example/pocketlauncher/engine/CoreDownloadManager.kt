package com.example.pocketlauncher.engine

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

data class CoreInstallResult(
    val path: String,
    val abi: String,
)

class CoreDownloadManager(
    private val context: Context,
) {
    fun installedCorePath(): String? {
        val file = coreFileForAbi(resolveAbi() ?: return null)
        return if (file.exists() && file.length() > 0) file.absolutePath else null
    }

    fun installedAbi(): String? =
        installedCorePath()?.let { resolveAbi() }

    suspend fun installMgba(): Result<CoreInstallResult> = withContext(Dispatchers.IO) {
        runCatching {
            val abi = resolveAbi()
                ?: error("This device ABI is not supported by the mGBA test channel.")

            val url = URL(
                "https://buildbot.libretro.com/nightly/android/latest/$abi/" +
                    "mgba_libretro_android.so.zip"
            )

            val target = coreFileForAbi(abi)
            target.parentFile?.mkdirs()

            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
                requestMethod = "GET"
            }

            connection.connect()
            if (connection.responseCode !in 200..299) {
                error("Core download failed with HTTP ${connection.responseCode}")
            }

            val temp = File(context.cacheDir, "mgba-core.zip")
            connection.inputStream.use { input ->
                temp.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            connection.disconnect()

            extractCore(temp, target)
            temp.delete()

            if (!target.exists() || target.length() == 0L) {
                error("mGBA core archive did not contain a usable .so file")
            }

            target.setReadable(true, true)
            target.setExecutable(true, true)

            CoreInstallResult(
                path = target.absolutePath,
                abi = abi,
            )
        }
    }

    fun removeMgba(): Boolean {
        var removed = false
        supportedAbis.forEach { abi ->
            val file = coreFileForAbi(abi)
            if (file.exists()) {
                removed = file.delete() || removed
            }
        }
        return removed
    }

    private fun extractCore(zipFile: File, target: File) {
        ZipInputStream(BufferedInputStream(zipFile.inputStream())).use { zip ->
            var entry = zip.nextEntry
            var found = false

            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".so", ignoreCase = true)) {
                    target.outputStream().use { output ->
                        zip.copyTo(output)
                    }
                    found = true
                    break
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }

            if (!found) {
                error("No native core library found in downloaded archive")
            }
        }
    }

    private fun coreFileForAbi(abi: String): File =
        File(
            File(context.filesDir, "cores/$abi"),
            "mgba_libretro_android.so",
        )

    private fun resolveAbi(): String? =
        Build.SUPPORTED_ABIS.firstOrNull { it in supportedAbis }

    private companion object {
        val supportedAbis = setOf(
            "arm64-v8a",
            "armeabi-v7a",
            "x86_64",
            "x86",
        )
    }
}
