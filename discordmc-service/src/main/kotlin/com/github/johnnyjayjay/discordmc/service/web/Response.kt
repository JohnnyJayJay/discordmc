package com.github.johnnyjayjay.discordmc.service.web

import com.github.johnnyjayjay.discordmc.service.klaxon
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText

interface JsonResponse {
    val code: Int
}

object SuccessResponse: JsonResponse {
     override val code = HttpStatusCode.OK.value
}

data class KeyResponse(val key: String, override val code: Int = HttpStatusCode.OK.value):
    JsonResponse

data class ErrorResponse(override val code: Int, val message: String):
    JsonResponse

suspend fun ApplicationCall.respond(body: ResponseBuilder.() -> Unit) {
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