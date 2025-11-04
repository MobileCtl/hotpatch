package com.mobilectl.hotpatch_poc.service

import com.mobilectl.hotpatch_poc.CartItem

class PaymentService {
    fun calculateTotal(items: List<CartItem>): Double {
        // INTENTIONAL BUG: Missing tax
        return items.sumOf { it.price * it.quantity }
    }
}