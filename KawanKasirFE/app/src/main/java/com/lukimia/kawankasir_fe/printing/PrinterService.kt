package com.lukimia.kawankasir_fe.printing

import org.json.JSONObject

interface PrinterService {
    fun preview(receipt: JSONObject): String
    fun print(receipt: JSONObject): PrintResult
    fun share(receipt: JSONObject): String
}

data class PrintResult(
    val success: Boolean,
    val message: String,
)

class ThermalPrinterPlaceholderService : PrinterService {
    override fun preview(receipt: JSONObject): String {
        val data = receipt.optJSONObject("data") ?: receipt
        val items = data.optJSONArray("items")
        val builder = StringBuilder()
        builder.appendLine(data.optString("store", "Kawan Kasir"))
        builder.appendLine(data.optString("invoice", "Preview Struk"))
        builder.appendLine("------------------------------")
        if (items != null) {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index)
                builder.appendLine("${item.optString("productName")} x${item.optInt("quantity")}  ${item.optString("subtotal")}")
            }
        }
        builder.appendLine("------------------------------")
        builder.appendLine("TOTAL  ${data.optString("total")}")
        builder.appendLine("BAYAR  ${data.optString("paid")}")
        builder.appendLine("KEMBALI ${data.optString("change")}")
        builder.appendLine(data.optString("footer", "Terima kasih"))
        return builder.toString()
    }

    override fun print(receipt: JSONObject): PrintResult = PrintResult(
        success = false,
        message = "Printer thermal belum dipasangkan. Service ini menjadi abstraction untuk Bluetooth/USB/network printer.",
    )

    override fun share(receipt: JSONObject): String = preview(receipt)
}
