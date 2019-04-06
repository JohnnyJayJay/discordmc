package com.github.johnnyjayjay.discordmc.service.web

import com.auth0.jwt.JWT
import com.github.johnnyjayjay.discordmc.service.*
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.authorization
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select

object Endpoints {
    suspend fun register(ctx: PipelineContext<Unit, ApplicationCall>) {
        val call = ctx.call
        val body = call.receiveOrNull<RegisterBody>()
        if (body == null) {
            call.respond {
                status = HttpStatusCode.BadRequest
                jsonResponse = ErrorResponse(
                    HttpStatusCode.BadRequest.value,
                    "No json object with guid and guild id provided"
                )
            }
        } else if (!Verification.exists(code = body.code)) {
            call.respond {
                status = HttpStatusCode.NotAcceptable
                jsonResponse = ErrorResponse(
                    HttpStatusCode.NotAcceptable.value,
                    "Verification code '${body.code}' does not exist"
                )
            }
        } else {
            val guid = body.guid
            val guildId = Verification.associatedGuild(body.code)
            Servers.deleteWhere {
                Servers.guildId.eq(guildId) or Servers.guid.eq(guid)
            }

            val token = JwtAuth.createToken()

            Servers.insert {
                it[Servers.guid] = guid
                it[Servers.guildId] = guildId
                it[authId] = JWT.decode(token).id
            }

            call.respond {
                jsonResponse = KeyResponse(token)
            }
        }
    }

    @KtorExperimentalAPI
    suspend fun postMessage(ctx: PipelineContext<Unit, ApplicationCall>) {
        val call = ctx.call
        val post = call.receive<PostMessageBody>()
        val authId = call.request.authorization()!!.toToken().id
        val row = Servers.select {
            Servers.authId eq authId
        }.first()

        val context = MessageContext(
            Server.fromResultRow(row), post
        ) {
            when (it) {
                MessageState.SENT ->
                    call.respond {
                        jsonResponse = SuccessResponse
                    }
                MessageState.NO_WEBHOOK,
                MessageState.NO_CHANNEL ->
                    call.respond {
                        status = HttpStatusCode.NotAcceptable
                        jsonResponse = ErrorResponse(
                            HttpStatusCode.NotAcceptable.value,
                            "Sending message to text channel failed: " +
                                    if (it == MessageState.NO_WEBHOOK) "webhook was deleted"
                                    else "text channel does not exist"
                        )
                    }
            }


        }
        Messages.sendMessage(context)
    }

    private fun String.toToken() = JWT.decode(this)
}