package com.mobilectl.hotpatch_poc

import org.keplerproject.luajava.LuaState
import org.keplerproject.luajava.LuaStateFactory
import org.keplerproject.luajava.LuaException

/**
 * Wrapper around LuaJava that provides a clean, simple API
 *
 * This translates your custom wrapper API to the underlying LuaJava library.
 * Instead of calling JNI directly, we use LuaJava's pre-built Kotlin/Java API.
 */
object LuaRuntime {

    // Thread-safe storage for Lua states
    private val states = mutableMapOf<Long, LuaState>()
    private var stateCounter: Long = 0

    init {
        try {
            System.loadLibrary("luajava")
            println("✅ Successfully loaded libluajava.so")
        } catch (e: UnsatisfiedLinkError) {
            println("❌ FAILED to load libluajava.so!")
            println("Error: ${e.message}")
            throw e
        }
    }

    /**
     * Create a new Lua state
     * @return State ID (positive number) or 0 on failure
     */
    fun newState(): Long {
        return try {
            val luaState = LuaStateFactory.newLuaState()
            luaState.openLibs()

            // Generate unique ID for this state
            val stateId = ++stateCounter
            states[stateId] = luaState
            stateId
        } catch (e: Exception) {
            println("❌ Failed to create Lua state: ${e.message}")
            0
        }
    }

    /**
     * Load Lua code string
     * @param state State ID from newState()
     * @param code Lua source code
     * @return 0 if successful, non-zero if error
     */
    fun loadString(state: Long, code: String): Int {
        return try {
            val luaState = states[state] ?: return -1

            // Load the code
            val loadResult = luaState.LloadString(code)
            println("DEBUG loadString: loadResult=$loadResult")

            if (loadResult != 0) {
                println("❌ Failed to load: ${luaState.toString(-1)}")
                return loadResult
            }

            // Execute the loaded code
            val callResult = luaState.pcall(0, 0, 0)
            println("DEBUG loadString: pcall result=$callResult")

            if (callResult != 0) {
                println("❌ Failed to execute: ${luaState.toString(-1)}")
                return callResult
            }

            println("✅ Function defined successfully")
            return 0

        } catch (e: Exception) {
            println("❌ Exception in loadString: ${e.message}")
            e.printStackTrace()
            return -1
        }
    }


    /**
     * Call a Lua function
     * @param state State ID
     * @param funcName Function name
     * @param args Arguments to pass
     * @return Return value from function
     */
    fun callFunction(state: Long, funcName: String, args: Array<Any?>): Any? {
        return try {
            val luaState = states[state] ?: return null

            // Debug: Check if function exists
            luaState.getGlobal(funcName)
            val functionExists = !luaState.isNil(-1)
            println("DEBUG: Looking for function '$funcName', exists: $functionExists")

            if (!functionExists) {
                println("DEBUG: Function not found! Available globals:")
                luaState.getGlobal("_G")  // Get global table
                // This would show all globals
                luaState.pop(1)
                return null
            }

            luaState.pop(1)  // Remove from stack

            // Re-get the function
            luaState.getGlobal(funcName)

            // Push arguments
            for (arg in args) {
                when (arg) {
                    is Number -> luaState.pushNumber(arg.toDouble())
                    is String -> luaState.pushString(arg)
                    is Boolean -> luaState.pushBoolean(arg)
                    null -> luaState.pushNil()
                    else -> luaState.pushString(arg.toString())
                }
            }

            // Call function
            luaState.pcall(args.size, 1, 0)

            // Get return value
            val result = when {
                luaState.isNumber(-1) -> luaState.toNumber(-1)
                luaState.isString(-1) -> luaState.toString(-1)
                luaState.isBoolean(-1) -> luaState.toBoolean(-1)
                luaState.isNil(-1) -> null
                else -> luaState.toString(-1)
            }

            luaState.pop(1)
            result
        } catch (e: Exception) {
            println("❌ Failed to call function: ${e.message}")
            e.printStackTrace()
            null
        }
    }


    /**
     * Execute Lua code directly
     * @param state State ID
     * @param code Lua source code to execute
     * @return 0 if successful, non-zero if error
     */
    fun doString(state: Long, code: String): Int {
        return try {
            val luaState = states[state] ?: return -1
            val result = luaState.LloadString(code)
            if (result == 0) {
                luaState.pcall(0, 0, 0)
            }
            result
        } catch (e: Exception) {
            println("❌ Failed to execute: ${e.message}")
            -1
        }
    }

    /**
     * Get a global variable from Lua
     * @param state State ID
     * @param name Variable name
     * @return Variable value
     */
    fun getGlobal(state: Long, name: String): Any? {
        return try {
            val luaState = states[state] ?: return null
            luaState.getGlobal(name)

            val result = when {
                luaState.isNumber(-1) -> luaState.toNumber(-1)
                luaState.isString(-1) -> luaState.toString(-1)
                luaState.isBoolean(-1) -> luaState.toBoolean(-1)
                luaState.isNil(-1) -> null
                else -> luaState.toString(-1)
            }

            luaState.pop(1)
            result
        } catch (e: Exception) {
            println("❌ Failed to get global: ${e.message}")
            null
        }
    }

    /**
     * Set a global variable in Lua
     * @param state State ID
     * @param name Variable name
     * @param value Variable value
     */
    fun setGlobal(state: Long, name: String, value: Any?) {
        try {
            val luaState = states[state] ?: return

            when (value) {
                is Number -> luaState.pushNumber(value.toDouble())
                is String -> luaState.pushString(value)
                is Boolean -> luaState.pushBoolean(value)
                null -> luaState.pushNil()
                else -> luaState.pushString(value.toString())
            }

            luaState.setGlobal(name)
        } catch (e: Exception) {
            println("❌ Failed to set global: ${e.message}")
        }
    }

    /**
     * Close Lua state and free memory
     * @param state State ID
     */
    fun closeState(state: Long) {
        try {
            val luaState = states.remove(state)
            luaState?.close()
        } catch (e: Exception) {
            println("❌ Failed to close state: ${e.message}")
        }
    }

    /**
     * Get error message from last operation
     * @param state State ID
     * @return Error message
     */
    fun getError(state: Long): String {
        return try {
            val luaState = states[state] ?: return "State not found"
            luaState.toString(-1)
        } catch (e: Exception) {
            e.message ?: "Unknown error"
        }
    }
}
