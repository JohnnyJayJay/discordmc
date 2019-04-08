package com.github.johnnyjayjay.discordmc.service.web

import com.auth0.jwt.JWT
import com.github.johnnyjayjay.discordmc.service.*
import com.github.johnnyjayjay.discordmc.service.bot.Bot
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.authorization
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select

@KtorExperimentalAPI
object Endpoints {

    suspend fun detach(ctx: PipelineContext<Unit, ApplicationCall>) {
        val call = ctx.call
        val authId = call.request.authorization()!!.toToken().id
        Servers.deleteWhere {
            Servers.authId eq authId
        }
        call.respond(HttpStatusCode.OK)
    }

    suspend fun register(ctx: PipelineContext<Unit, ApplicationCall>) {
        val call = ctx.call
        val body = call.receiveOrNull<RegisterBody>()
        if (body == null) {
            call.respond(HttpStatusCode.BadRequest)
        } else if (!Verification.exists(code = body.code)) {
            call.respond(HttpStatusCode.NotAcceptable)
        } else {
            val code = body.code
            val serverId = body.serverId
            val guildId = Verification.associatedGuild(code)
            Verification.invalidate(code)
            Servers.deleteWhere {
                Servers.guildId.eq(guildId) or Servers.guid.eq(serverId)
            }

            val token = JwtAuth.createToken()

            Servers.insert {
                it[Servers.guid] = serverId
                it[Servers.guildId] = guildId
                it[authId] = token.toToken().id
            }

            call.respondJson {
                status = HttpStatusCode.OK
                jsonResponse = KeyResponse(token, Guild(Bot.getGuildName(guildId)!!, guildId))
            }
        }
    }

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
                    call.respond(HttpStatusCode.OK)
                MessageState.NO_WEBHOOK,
                MessageState.NO_CHANNEL ->
                    call.respond(HttpStatusCode.NotAcceptable)
            }


        }
        Messages.sendMessage(context)
    }

    suspend fun linkInfo(ctx: PipelineContext<Unit, ApplicationCall>) {
        val call = ctx.call
        val authId = call.request.authorization()!!.toToken().id
        val jsonResponse = Servers.select {
            Servers.authId eq authId
        }.first().let {
            val guildId = it[Servers.guildId]
            InfoResponse(
                guild = Guild(Bot.getGuildName(guildId)!!, guildId),
                linkedChannel = Bot.getChannel(guildId, it[Servers.channelId])?.name
            )
        }
        call.respondJson {
            status = HttpStatusCode.OK
            this.jsonResponse = jsonResponse
        }
    }

    private fun String.toToken() = JWT.decode(this.replace("Bearer ", ""))
}
