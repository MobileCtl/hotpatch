package com.mobilectl.hotpatch.service

import android.content.Context
import kotlinx.coroutines.*
import java.io.File
import java.net.URL
import org.json.JSONObject

object PatchDownloader {

    private lateinit var context: Context

    private const val GITHUB_OWNER = "mobilectl"
    private const val GITHUB_REPO = "hotpatch"
    private const val GITHUB_RAW_URL =
        "https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/master"

    fun init(ctx: Context) {
        context = ctx
    }

    /**
     * Check manifest.json for patch metadata
     */
    suspend fun checkForPatches(methodName: String): PatchMetadata? {
        return withContext(Dispatchers.IO) {
            try {
                println("🔍 Checking for patch: $methodName")

                val manifestUrl = "$GITHUB_RAW_URL/patches/manifest.json"
                val manifestJson = downloadFile(manifestUrl)

                val json = JSONObject(manifestJson)

                if (!json.has(methodName)) {
                    println("⚠️ No patch found for $methodName in manifest")
                    return@withContext null
                }

                val patchJson = json.getJSONObject(methodName)

                val metadata = PatchMetadata(
                    methodName = methodName,
                    version = patchJson.getString("version"),
                    enabled = patchJson.optBoolean("enabled", true),
                    file = patchJson.getString("file"),
                    size = patchJson.getLong("size")
                )

                println("✅ Found patch: $metadata")
                return@withContext metadata
            } catch (e: Exception) {
                println("❌ Failed to check patches: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Download patch file from GitHub
     */
    suspend fun downloadPatch(metadata: PatchMetadata): String? {
        return withContext(Dispatchers.IO) {
            try {
                println("📥 Downloading patch: ${metadata.methodName} v${metadata.version}")

                val cachedFile = getCachedPatchFile(metadata.methodName, metadata.version)
                if (cachedFile.exists()) {
                    println("✅ Using cached patch")
                    return@withContext cachedFile.readText()
                }

                val patchUrl = "$GITHUB_RAW_URL/patches/${metadata.file}"
                val luaCode = downloadFile(patchUrl)

                println("✅ Downloaded ${luaCode.length} bytes")

                // Cache
                cachedFile.writeText(luaCode)
                println("💾 Cached to ${cachedFile.absolutePath}")

                luaCode
            } catch (e: Exception) {
                println("❌ Failed to download patch: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    private fun downloadFile(urlString: String): String {
        println("🌐 Downloading from: $urlString")
        return URL(urlString).readText(Charsets.UTF_8)
    }

    private fun getCachedPatchFile(methodName: String, version: String): File {
        val patchDir = File(context.cacheDir, "patches")
        patchDir.mkdirs()
        return File(patchDir, "$methodName-v$version.lua")
    }

    fun clearCache() {
        val patchDir = File(context.cacheDir, "patches")
        patchDir.deleteRecursively()
        println("✅ Patch cache cleared")
    }
}

/**
 * Metadata about a patch
 */
data class PatchMetadata(
    val methodName: String,
    val version: String,
    val enabled: Boolean,
    val file: String,
    val size: Long
)
