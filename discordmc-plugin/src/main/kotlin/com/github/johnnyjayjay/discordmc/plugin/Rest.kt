package com.github.johnnyjayjay.discordmc.plugin

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.google.gson.JsonParser
import khttp.delete
import khttp.get
import khttp.post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Requests {
    private const val baseURL = ""

    private val jsonParser = JsonParser()
    private val klaxon = Klaxon()

    private var authHeader: Map<String, String> = emptyMap()

    var token: String? = null
        set(value) {
            field = value
            authHeader = mapOf("Authorization" to "Bearer $token")
        }

    fun register(serverId: String, verification: Int): Pair<String?, Guild>? {
        val response = post(
            url = "$baseURL/register", json = """
            {
                "serverId": $serverId,
                "code": $verification
            }
        """.trimIndent()
        )
        return if (response.statusCode!= 200) null
        else response.jsonObject.getString("token") to
                Guild(response.jsonObject.getString("name"), response.jsonObject.getLong("id"))
    }

    fun postMessage(author: String, content: String): Boolean {
        val response = post(
            url = "$baseURL/message",
            json = """
            {
                "author": $author,
                "content": $content
            }
        """.trimIndent(),
            headers = authHeader
        )
        return response.statusCode == 200
    }

    fun detach(): Boolean {
        val response = delete(
            url = "$baseURL/detach",
            headers = authHeader
        )
        return response.statusCode == 200
    }

    fun getInfo(): Info? {
        val response = get(
            url = "$baseURL/info",
            headers = authHeader
        )
        return if (response.statusCode == 200) klaxon.parse(response.text) else null
    }

    fun async(body: Requests.() -> Unit) =
        GlobalScope.launch {
            body()
        }

}

data class Guild(val name: String, val id: Long)

data class Info(
    val guild: Guild,
    @Json(name = "linked_channel")
    val linkedChannel: String? = null
)

