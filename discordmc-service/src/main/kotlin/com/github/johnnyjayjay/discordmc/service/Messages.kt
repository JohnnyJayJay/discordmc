package com.github.johnnyjayjay.discordmc.service

import com.github.johnnyjayjay.discordmc.service.bot.Bot
import com.github.johnnyjayjay.discordmc.service.web.PostMessageBody
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.dv8tion.jda.core.entities.Icon
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.webhook.WebhookClient
import net.dv8tion.jda.webhook.WebhookClientBuilder
import net.dv8tion.jda.webhook.WebhookMessageBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object Messages {

    val webhookIcon = Icon.from(Messages::class.java.getResourceAsStream("/icon.png"))

    private val webhookClients = mutableMapOf<Long, WebhookClient>()

    suspend fun sendMessage(ctx: MessageContext) {
        val (guildId, guid, authId, channelId) = ctx.server
        val (content, author) = ctx.message
        val channel = Bot.getChannel(guildId, channelId)
        if (channel == null) {
            ctx.callback(MessageState.NO_CHANNEL)
            return
        }

        val client = webhookClients[guildId]
        if (client == null) {
            insertClientAndSend(channel, ctx)
            return
        }

        client.send(WebhookMessageBuilder().setUsername(author).setContent(content).build()).await()
        ctx.callback(MessageState.SENT)
    }

    private suspend fun insertClientAndSend(channel: TextChannel, ctx: MessageContext) {
        val webhooks = channel.webhooks.submit().await()
        val guidString = ctx.server.guid.toString()
        val webhook = webhooks.firstOrNull { it.name == guidString }
        if (webhook == null) {
            channel.sendMessage(
                "Minecraft messages may not be received due to unexpected changes to important parts " +
                        "of this channel\nPlease use `${Bot.prefix}channel` again, @Administrator"
            ).queue()
            Servers.update(where = {
                Servers.authId eq ctx.server.authId
            }, body = { statement ->
                statement[channelId] = 0L
            })
            GlobalScope.launch {
                ctx.callback(MessageState.NO_WEBHOOK)
            }
        } else {
            val client = WebhookClientBuilder(webhook).build()
            webhookClients[ctx.server.guildId] = client
            sendMessage(ctx)
        }
    }

    fun TextChannel.createWebhook(guildId: Long) {
        createWebhook(
            Servers.select {
                Servers.guildId eq guildId
            }.first()[Servers.guid].toString()
        ).setAvatar(Messages.webhookIcon).queue()
    }
}

data class MessageContext(
    val server: Server,
    val message: PostMessageBody,
    val callback: suspend (MessageState) -> Unit
)

enum class MessageState {
    SENT, NO_WEBHOOK, NO_CHANNEL
}