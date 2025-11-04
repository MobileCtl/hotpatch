package com.mobilectl.hotpatch.service

import com.mobilectl.hotpatch.CartItem

class PaymentService {
    fun calculateTotal(items: List<CartItem>): Double {
        // INTENTIONAL BUG: Missing tax
        return items.sumOf { it.price * it.quantity }
    }
}