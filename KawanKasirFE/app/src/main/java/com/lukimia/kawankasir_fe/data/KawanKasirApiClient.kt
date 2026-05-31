package com.lukimia.kawankasir_fe.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class KawanKasirApiClient(
    private val baseUrl: String = "http://10.0.2.2:8080",
) {
    var accessToken: String? = null
        private set

    fun login(usernameOrEmail: String, password: String): JSONObject {
        val response = post("/api/auth/login", JSONObject().apply {
            put("usernameOrEmail", usernameOrEmail)
            put("password", password)
        })
        accessToken = response.optJSONObject("data")?.optString("accessToken")
        return response
    }

    fun openShift(outletId: String, cashierId: String, openingCash: Long): JSONObject = post("/api/shifts/open", JSONObject().apply {
        put("outletId", outletId)
        put("cashierId", cashierId)
        put("openingCash", openingCash)
    })

    fun searchProducts(keyword: String): JSONObject = get("/api/products/search?keyword=${keyword.urlEncode()}")

    fun createSale(outletId: String, cashierId: String, items: JSONArray, paidAmount: Long): JSONObject = post("/api/sales", JSONObject().apply {
        put("outletId", outletId)
        put("cashierId", cashierId)
        put("items", items)
        put("paymentMethod", "CASH")
        put("paidAmount", paidAmount)
    })

    fun receipt(saleId: String): JSONObject = get("/api/sales/$saleId/receipt")

    private fun get(path: String): JSONObject = request("GET", path, null)
    private fun post(path: String, payload: JSONObject): JSONObject = request("POST", path, payload)

    private fun request(method: String, path: String, payload: JSONObject?): JSONObject {
        val connection = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            accessToken?.let { setRequestProperty("Authorization", "Bearer $it") }
            doInput = true
        }
        if (payload != null) {
            connection.doOutput = true
            OutputStreamWriter(connection.outputStream).use { it.write(payload.toString()) }
        }
        val body = runCatching {
            connection.inputStream.bufferedReader().use(BufferedReader::readText)
        }.getOrElse {
            connection.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: "{}"
        }
        val json = JSONObject(body.ifBlank { "{}" })
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(json.optString("message", "Request gagal (${connection.responseCode})"))
        }
        return json
    }

    private fun String.urlEncode(): String = java.net.URLEncoder.encode(this, "UTF-8")
}
