package com.mobilectl.hotpatch.patch

import android.content.Context
import com.mobilectl.hotpatch.CartItem
import com.mobilectl.hotpatch.LuaRuntime
import com.mobilectl.hotpatch.service.PaymentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages hotpatching runtime
 *
 * Intercepts method calls and replaces them with Lua versions
 */
object PatchRuntime {

    private var shouldPatch = false
    private var luaState: Long = 0

    /**
     * Initialize the patch runtime
     */
    init {
        try {
            luaState = LuaRuntime.newState()
            println("✅ PatchRuntime initialized")
        } catch (e: Exception) {
            println("❌ Failed to init PatchRuntime: ${e.message}")
        }
    }

    /**
     * Enable/disable patching
     */
    fun enablePatching(enable: Boolean, context: Context) {
        shouldPatch = enable

        if (enable) {
            // Load Lua patch when enabled
            loadLuaPatch(context)
        }
    }

    /**
     * Check if we should intercept this method
     */
    fun shouldIntercept(methodName: String): Boolean {
        return shouldPatch && methodName == "calculateTotal"
    }

    /**
     * Load the Lua patch code
     */
    private fun loadLuaPatch(context: Context) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                PatchLoader.loadFromUrl(context).also { luaCode ->
                    luaCode?.let { LuaRuntime.loadString(luaState, it) }
                }
            }

            println("✅ Lua patch loaded")
        } catch (e: Exception) {
            println("❌ Failed to load Lua patch: ${e.message}")
        }
    }


    /**
     * Execute patched version of calculateTotal
     */
    fun executePatched(service: PaymentService, items: List<CartItem>): Double {
        return try {
            if (!shouldPatch) {
                return service.calculateTotal(items)
            }

            // Convert items to JSON string
            val jsonArray = JSONArray()
            items.forEach { item ->
                val obj = JSONObject()
                obj.put("name", item.name)
                obj.put("price", item.price)
                obj.put("quantity", item.quantity)
                jsonArray.put(obj)
            }

            val itemsJson = jsonArray.toString()
            println("DEBUG: Passing JSON: $itemsJson")

            // Pass JSON string to Lua
            val result = LuaRuntime.callFunction(
                luaState,
                "calculateTotalFromJson",
                arrayOf(itemsJson)
            )

            when (result) {
                is Number -> result.toDouble()
                else -> service.calculateTotal(items)
            }
        } catch (e: Exception) {
            println("❌ Patch failed: ${e.message}")
            service.calculateTotal(items)
        }
    }

    /**
     * Cleanup when app closes
     */
    fun cleanup() {
        try {
            LuaRuntime.closeState(luaState)
        } catch (e: Exception) {
            println("❌ Failed to cleanup: ${e.message}")
        }
    }
}