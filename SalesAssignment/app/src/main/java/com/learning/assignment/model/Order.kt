package com.learning.assignment.model

data class Order(
    val orderID: Int,
    val discount: String,
    val items: List<Item>
)


