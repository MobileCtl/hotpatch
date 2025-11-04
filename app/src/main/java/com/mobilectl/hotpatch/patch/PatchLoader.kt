package com.mobilectl.hotpatch.patch

import android.content.Context
import com.mobilectl.hotpatch.service.PatchDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PatchLoader {

    /**
     * Load patch from app assets
     */
    fun loadPatchFromAssets(context: Context, fileName: String): String? {
        return try {
            println("📦 Loading patch from assets: $fileName")
            context.assets.open("patches/$fileName")
                .bufferedReader()
                .use { it.readText() }
                .also { println("✅ Loaded ${it.length} bytes from assets") }
        } catch (e: Exception) {
            println("❌ Failed to load from assets: ${e.message}")
            null
        }
    }

    /**
     * Load patch from remote URL (GitHub)
     */
    suspend fun loadFromUrl(context: Context, methodName: String = "calculateTotal"): String? {
        return withContext(Dispatchers.IO) {
            try {
                println("🔄 Fetching patch from GitHub...")

                PatchDownloader.init(context)

                val metadata = PatchDownloader.checkForPatches(methodName)
                    ?: run {
                        println("⚠️ No patch found for $methodName")
                        return@withContext null
                    }

                if (!metadata.enabled) {
                    println("⚠️ Patch is disabled in manifest")
                    return@withContext null
                }

                // Download patch
                val luaCode = PatchDownloader.downloadPatch(metadata)
                    ?: run {
                        println("❌ Failed to download patch")
                        return@withContext null
                    }

                println("✅ Patch loaded from GitHub (${luaCode.length} bytes)")
                luaCode

            } catch (e: Exception) {
                println("❌ Failed to load patch: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Load with fallback: try assets first, then remote
     */
    suspend fun loadWithFallback(
        context: Context,
        fileName: String,
        methodName: String = "calculateTotal"
    ): String? {
        loadPatchFromAssets(context, fileName)?.let {
            println("✅ Using patch from assets")
            return it
        }

        println("⚠️ Patch not in assets, trying remote...")

        return loadFromUrl(context, methodName)?.also {
            println("✅ Using patch from remote")
        }
    }
}
