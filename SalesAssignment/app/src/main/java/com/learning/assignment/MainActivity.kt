package com.learning.assignment

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.learning.assignment.model.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val salesProfitTv = findViewById<TextView>(R.id.salesProfitTv)

        //For read the discount json file
        val discountJsonString = readJSONFromAssets("discounts.json")
        val discounts: Array<Discount> = Gson().fromJson(
            discountJsonString,
            Array<Discount>::class.java
        )
        //For read the discounts json file
        val orderJsonString = readJSONFromAssets("orders.json")
        val orders: Array<Order> = Gson().fromJson(
            orderJsonString,
            Array<Order>::class.java
        )

        //For read the products json file
        val productsJsonString = readJSONFromAssets("products.json")
        val products: Array<Product> = Gson().fromJson(
            productsJsonString,
            Array<Product>::class.java
        )

        calculateSales(orders.toList(), products.toList(), discounts.toList(), salesProfitTv)
    }

    /**
     * to calculate sale for the product
     */
    private fun calculateSales(
        orderList: List<Order>,
        productList: List<Product>,
        discountList: List<Discount>,
        salesProfitTv: TextView,
    ) {
        var totalSales = 0.0
        var totalLossAfterDiscount = 0.0
        var totalDiscount = 0.0
        val productMap = createProductMap(productList)
        val discountMap = createDiscountMap(discountList)
        for (order in orderList) {
            var currentSales = 0.0
            var discountedSales = 0.0
            for (eachItem in order.items) {
                val price = productMap[eachItem.sku]
                currentSales += price?.times(eachItem.quantity.toDouble()) ?: 0.0
            }

            if (order.discount != null) {
                totalDiscount += calculatedOrderDiscount(order.discount, discountMap)
                discountedSales =
                    currentSales * calculatedOrderDiscount(order.discount, discountMap)
            }
            totalSales += currentSales
            totalLossAfterDiscount += discountedSales
        }
        val totalSalesAfterDiscount = totalSales - totalLossAfterDiscount
        salesProfitTv.setText(
            getString(R.string.total_sales) + totalSales
                    + "\n" + getString(R.string.after_discount_sales) + totalSalesAfterDiscount
                    + "\n" + getString(R.string.loss_sales) + totalLossAfterDiscount
                    + "\n" + getString(R.string.average_discount) + totalDiscount / orderList.size.toDouble()
        )
        println("Total Sales : $totalSales")
        println("Total Sales after discount : $totalSalesAfterDiscount")
        println("Total loss due to discount : $totalLossAfterDiscount")
        println("Average discount : " + totalDiscount / orderList.size.toDouble())
    }

    /**
     * Create map for Product list to optimize product search
     */

    private fun createProductMap(products: List<Product>): MutableMap<Int, Double> {
        val productMap = mutableMapOf<Int, Double>()
        for (product in products) {
            productMap.put(product.sku, product.price)
        }
        return productMap
    }

    /**
     * Create map for Discount list to optimize discount search
     */
    private fun createDiscountMap(discounts: List<Discount>): MutableMap<String, Double> {
        val discountMap = mutableMapOf<String, Double>()
        for (discount in discounts) {
            discountMap.put(discount.key, discount.value)
        }
        discountMap.put("", 0.0)
        return discountMap
    }

    /**
     * Read json file from assets folder
     */
    private fun readJSONFromAssets(filename: String): String {
        return applicationContext.assets.open(filename).bufferedReader().use { reader ->
            reader.readText()
        }
    }

    /**
     * Calculate total discount for order
     */
    private fun calculatedOrderDiscount(
        discount: String,
        discountMap: MutableMap<String, Double>
    ): Double {
        val discountCodeList = discount.split(",")
        var totalDiscount = 0.0
        for (discountCode in discountCodeList) {
            totalDiscount += (discountMap[discountCode] ?: 0.0)
        }
        return totalDiscount
    }

}

