package com.mobilectl.hotpatch_poc.patch

import com.mobilectl.hotpatch_poc.CartItem
import com.mobilectl.hotpatch_poc.LuaRuntime
import com.mobilectl.hotpatch_poc.service.PaymentService
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
    fun enablePatching(enable: Boolean) {
        shouldPatch = enable

        if (enable) {
            // Load Lua patch when enabled
            loadLuaPatch()
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
    private fun loadLuaPatch() {
        try {
            val luaCode = """
            -- Simple JSON parser for Lua
            function decodeJson(jsonStr)
                -- Remove brackets and split
                local items = {}
                -- This is simplified - in production use a real JSON library
                for match in jsonStr:gmatch('{[^}]*}') do
                    table.insert(items, match)
                end
                return items
            end
            
            function calculateTotalFromJson(jsonStr)
                print("DEBUG Lua: received JSON: " .. jsonStr)
                
                -- Parse JSON manually (super simple)
                local total = 0.0
                
                -- Extract prices and quantities from JSON
                for price, qty in jsonStr:gmatch('"price":(%-?%d+%.?%d*)[^}]*"quantity":(%d+)') do
                    local itemTotal = tonumber(price) * tonumber(qty) * 1.15
                    total = total + itemTotal
                end
                
                print("DEBUG Lua: total=" .. total)
                return total
            end
        """

            LuaRuntime.loadString(luaState, luaCode)
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