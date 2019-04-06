package com.github.johnnyjayjay.discordmc.service.bot

import com.github.johnnyjayjay.discordmc.service.Messages.createWebhook
import com.github.johnnyjayjay.discordmc.service.Server
import com.github.johnnyjayjay.discordmc.service.Servers
import com.github.johnnyjayjay.discordmc.service.web.Verification
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.annotation.JDACommand
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update

@JDACommand.Module("registerCommand", "detachCommand")
object Commands {

    @JDACommand(
        name = ["register", "r"],
        cooldown = JDACommand.Cooldown(value = 60, scope = Command.CooldownScope.GUILD),
        userPermissions = [Permission.ADMINISTRATOR],
        help = "Use this command to retrieve a verification code which may then be used to connect " +
                "this server with your Minecraft server.\n" +
                "**Usage**: `${Bot.prefix}register`",
        botPermissions = [Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION]
    )
    fun registerCommand(event: CommandEvent) {
        if (Server.exists(event.guild.idLong)) {
            event.reply("This server is already linked to a Minecraft server!")
            event.reactWarning()
        } else {
            val code = Verification.insert(event.guild.idLong)
            event.replyInDm("The verification code for your Minecraft server is: `$code`.", {
                event.reactSuccess()
            }, {
                event.reactError()
                event.reply("I couldn't send you a private message. Are your DMs open?")
            })
        }
    }

    @JDACommand(
        name = ["detach"],
        userPermissions = [Permission.ADMINISTRATOR],
        botPermissions = [Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION],
        help = "Use this command to disconnect this Discord server from the linked Minecraft server (if there is one).\n" +
                "**Usage**: `${Bot.prefix}detach`"
    )
    fun detachCommand(event: CommandEvent) {
        val guildId = event.guild.idLong
        if (Server.exists(guildId)) {
            Servers.deleteWhere {
                Servers.guildId eq guildId
            }
            event.reactSuccess()
            event.reply("The link has successfully been deleted.")
        } else {
            event.reactWarning()
            event.reply("There is no Minecraft server linked to this Discord server!")
        }
    }

    @JDACommand(
        name = ["channel", "c"],
        userPermissions = [Permission.ADMINISTRATOR],
        botPermissions = [Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION],
        help = "Execute this command in a text channel to set it as the message channel for incoming Minecraft messages. " +
                "Only works if this Discord server is linked to a Minecraft server.\n" +
                "**Usage**: `${Bot.prefix}channel`"
    )
    fun channelCommand(event: CommandEvent) {
        val guildId = event.guild.idLong
        if (Server.exists(guildId)) {
            val channel = event.textChannel
            Servers.update(where = {
                Servers.guildId eq guildId
            }, body = {
                it[channelId] = channel.idLong
            })
            channel.createWebhook(guildId)
            event.reactSuccess()
            event.reply("This channel was successfully set to be the Minecraft message channel.")
        } else {
            event.reactWarning()
            event.reply("There is no Minecraft server linked to this Discord server!")
        }
    }


}