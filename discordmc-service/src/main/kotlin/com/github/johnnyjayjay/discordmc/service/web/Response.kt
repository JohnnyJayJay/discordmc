package com.github.johnnyjayjay.discordmc.service.web

import com.beust.klaxon.Json
import com.github.johnnyjayjay.discordmc.service.klaxon
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText

interface JsonResponse {
    val code: Int
}

data class Guild(val name: String, val id: Long)

data class KeyResponse(val key: String, val guild: Guild): JsonResponse {
    override val code: Int = HttpStatusCode.OK.value
}

data class InfoResponse(
    val guild: Guild,
    @Json(name = "linked_channel")
    val linkedChannel: String?
) : JsonResponse {
    override val code: Int = HttpStatusCode.OK.value
}

suspend inline fun ApplicationCall.respondJson(body: ResponseBuilder.() -> Unit) {
    val builder = ResponseBuilder()
    builder.body()
    builder.respond(this)
}

class ResponseBuilder {
    var status: HttpStatusCode = HttpStatusCode.OK
    lateinit var jsonResponse: JsonResponse

    suspend fun respond(call: ApplicationCall) {
        call.respondText(
            contentType = ContentType.Application.Json,
            status = status,
            text = klaxon.toJsonString(jsonResponse)
        )
    }
}