package com.github.johnnyjayjay.discordmc.service

import com.github.johnnyjayjay.discordmc.service.bot.Bot
import com.github.johnnyjayjay.discordmc.service.web.PostMessageBody
import kotlinx.coroutines.future.await
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

    suspend fun sendMessage(ctx: MessageContext) = with (ctx) {
        val (guildId, _, _, channelId) = server
        val (content, author) = message
        val channel = Bot.getChannel(guildId, channelId)
        if (channel == null) {
            callback(MessageState.NO_CHANNEL)
            return
        }

        var client = webhookClients[guildId]
        if (client == null) {
            val webhooks = channel.webhooks.submit().await()
            val guidString = server.serverId.toString()
            val webhook = webhooks.firstOrNull { it.name == guidString }
            if (webhook == null) {
                channel.sendMessage(
                    "Minecraft messages may not be received due to unexpected changes to important parts " +
                            "of this channel\nPlease use `${Bot.prefix}channel` again, @Administrator"
                ).queue()
                Servers.update(where = {
                    Servers.authId eq server.authId
                }, body = { statement ->
                    statement[Servers.channelId] = 0L
                })
                callback(MessageState.NO_WEBHOOK)
                return
            } else {
                client = WebhookClientBuilder(webhook).build()
                webhookClients[server.guildId] = client
            }
        }

        client!!.send(WebhookMessageBuilder().setUsername(author).setContent(content).build()).await()
        callback(MessageState.SENT)
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