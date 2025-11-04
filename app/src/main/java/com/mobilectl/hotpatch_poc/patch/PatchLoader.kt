package com.mobilectl.hotpatch_poc.patch

import android.content.Context

object PatchLoader {
    fun loadPatchFromAssets(context: Context, fileName: String): String {
        return context.assets.open("patches/$fileName")
            .bufferedReader()
            .use { it.readText() }
    }
}