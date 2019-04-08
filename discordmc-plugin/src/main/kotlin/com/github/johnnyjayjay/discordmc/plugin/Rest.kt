package com.github.johnnyjayjay.discordmc.plugin

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.google.gson.JsonParser
import com.sun.org.apache.xpath.internal.operations.Bool
import khttp.delete
import khttp.get
import khttp.post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object Requests {
    private const val baseURL = "http://localhost" // TODO

    private val klaxon = Klaxon()

    private var authHeader: Map<String, String> = emptyMap()

    var token: String? = TokenStash.token
        set(value) {
            field = value
            authHeader = mapOf("Authorization" to "Bearer $token")
        }

    fun register(serverId: String, verification: Int): Response<Pair<String, Guild>> {
        val response = post(
            url = "$baseURL/register", json = """
            {
                "serverId": $serverId,
                "code": $verification
            }
        """.trimIndent()
        )
        val jsonObject = response.jsonObject
        return Response(
            status = response.statusCode,
            content = jsonObject.getString("token")
                    to Guild(jsonObject.getString("name"), jsonObject.getLong("id"))
        )
    }

    fun postMessage(author: String, content: String): Response<Empty> {
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
        return Response(status = response.statusCode, content = Empty)
    }

    fun detach(): Response<Empty> {
        val response = delete(
            url = "$baseURL/detach",
            headers = authHeader
        )
        return Response(status = response.statusCode, content = Empty)
    }

    fun getInfo(): Response<Info> {
        val response = get(
            url = "$baseURL/info",
            headers = authHeader
        )
        return Response(status = response.statusCode, content = klaxon.parse(response.text))
    }

    fun <T> async(method: Requests.() -> Response<T>, body: Response<T>.() -> Unit) =
        GlobalScope.launch {
            method().body()
        }

}

object Empty

data class Response<T>(
    val status: Int,
    val content: T?
) {
    val errored: Boolean = status != 200
    val type: ResponseType = ResponseType.forCode(status)
}

enum class ResponseType(val code: Int) {
    RateLimit(419),
    NotLinked(401),
    NotAcceptable(406),
    OK(200),
    InternalError(400);

    companion object {
        fun forCode(code: Int) = values().first { it.code == code }
    }
}

data class Guild(val name: String, val id: Long)

data class Info(
    val guild: Guild,
    @Json(name = "linked_channel")
    val linkedChannel: String? = null
)

