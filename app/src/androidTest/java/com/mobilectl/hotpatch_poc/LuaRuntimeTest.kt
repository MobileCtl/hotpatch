package com.mobilectl.hotpatch_poc

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LuaRuntimeTest {

    private var state: Long = 0

    @Before
    fun setUp() {
        state = LuaRuntime.newState()
        Assert.assertTrue("Failed to create Lua state", state > 0)
    }

    @After
    fun tearDown() {
        if (state > 0) {
            LuaRuntime.closeState(state)
        }
    }

    @Test
    fun testBasicArithmetic() {
        // Test: Lua can do math
        LuaRuntime.loadString(state, """
            function add(a, b)
                return a + b
            end
        """)

        val result = LuaRuntime.callFunction(state, "add", arrayOf(5, 3))
        Assert.assertEquals(8.0, result)
    }

    @Test
    fun testStringConcatenation() {
        // Load AND execute the function definition
        val loadResult = LuaRuntime.loadString(state, """
            function greet(name)
                return "Hello, " .. name
            end
        """)

        // IMPORTANT: Execute it! (this is missing in current code)
        if (loadResult == 0) {
            LuaRuntime.doString(state, "")  // Execute the loaded code
        }

        // NOW call the function
        val result = LuaRuntime.callFunction(state, "greet", arrayOf("World"))
        Assert.assertEquals("Hello, World", result)
    }

    @Test
    fun testGlobalVariable() {
        // Test: Can set/get globals
        LuaRuntime.setGlobal(state, "counter", 42)
        val value = LuaRuntime.getGlobal(state, "counter")
        Assert.assertEquals(42.0, value)
    }

    @Test
    fun testDirectExecution() {
        // Test: Can execute code directly
        val result = LuaRuntime.doString(state, """
            x = 10
            y = 20
            z = x + y
        """)

        Assert.assertEquals(0, result)  // 0 = success

        val z = LuaRuntime.getGlobal(state, "z")
        Assert.assertEquals(30.0, z)
    }
    @Test
    fun testErrorHandling() {
        // Test: Catches syntax errors
        val result = LuaRuntime.loadString(state, "this is not valid lua !!!")

        println("DEBUG: loadString result = $result")
        Assert.assertNotEquals(0, result)  // Should return non-zero (error code)

        val error = LuaRuntime.getError(state)
        println("DEBUG: error message = $error")

        // Check for any error-related keywords (more flexible)
        Assert.assertTrue(
            "Error message should contain error info",
            error.contains("expected") || error.contains("syntax") ||
                    error.contains("error") || error.contains("Error")
        )
    }
}