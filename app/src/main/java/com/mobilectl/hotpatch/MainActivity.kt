package com.mobilectl.hotpatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobilectl.hotpatch.patch.PatchRuntime
import com.mobilectl.hotpatch.service.PaymentService
import com.mobilectl.hotpatch.ui.theme.HotpatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HotpatchTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        PaymentCalculatorScreen()
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        PatchRuntime.cleanup()
    }
}
@Composable
fun PaymentCalculatorScreen() {
    val service = remember { PaymentService() }
    var isPatchEnabled by remember { mutableStateOf(false) }
    var totalPrice by remember { mutableStateOf(0.0) }
    val context = LocalContext.current
    // Sample items
    val items = remember {
        listOf(
            CartItem("Item 1", 100.0, 2),
            CartItem("Item 2", 50.0, 1)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Payment Calculator",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Items Display
        Text(text = "Items:", fontSize = 18.sp)
        for (item in items) {
            Text("${item.name}: $${item.price} × ${item.quantity}")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Total Display
        Text(
            text = "Total: $${"%.2f".format(totalPrice)}",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status
        if (!isPatchEnabled) {
            Text(
                text = "❌ Bug: Missing 15% tax",
                fontSize = 14.sp
            )
        } else {
            Text(
                text = "✅ Patched: Tax included",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Calculate Button
        Button(
            onClick = {
                totalPrice = if (isPatchEnabled) {
                    PatchRuntime.executePatched(service, items)
                } else {
                    service.calculateTotal(items)
                }
            }
        ) {
            Text("Calculate Total")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Patch Toggle Button
        Button(
            onClick = {
                isPatchEnabled = !isPatchEnabled
                PatchRuntime.enablePatching(isPatchEnabled, context = context)
                if (isPatchEnabled) {
                    println("✅ Patching ENABLED")
                } else {
                    println("❌ Patching DISABLED")
                }
            }
        ) {
            Text(if (isPatchEnabled) "Disable Patch" else "Enable Patch")
        }
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HotpatchTheme {
        PaymentCalculatorScreen()
    }
}