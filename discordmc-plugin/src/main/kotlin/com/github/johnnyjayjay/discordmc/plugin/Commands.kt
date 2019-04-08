package com.github.johnnyjayjay.discordmc.plugin

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

object Commands {

    fun register(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val code = if (args.size == 1) args[0].toIntOrNull() ?: return false else return false
        @Suppress("DEPRECATION")
        val serverId = sender.server.serverId

        Requests.async({ register(serverId, code) }) {
            if (errored) {
                sender.sendMessage(
                    "§cCould not register a link for this server, because ${when (type) {
                        ResponseType.NotAcceptable -> "that verification code does not exist"
                        else -> "[unknown reason]. Please report this to the developers"
                    }}."
                )
            } else {
                val content = content!!
                TokenStash.token = content.first
                Requests.token = content.first
                val guild = content.second
                sender.sendMessage("§aSuccessfully connected this server with §6${guild.name} (${guild.id})§a!")
            }
        }
        return true
    }

    fun detach(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty())
            return false

        Requests.async({ detach() }) {
            sender.sendMessage(
                if (errored) "§cThere is no link to detach from!"
                else "§aThe link to the Discord server was successfully deleted."
            )
        }
        return true
    }

    fun discord(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty())
            return false

        val message = args.joinToString(" ")
        val author = sender.name
        Requests.async({ postMessage(author, message) }) {
            if (errored) {
                sender.sendMessage(
                    "§Your message could not be sent, because ${when (type) {
                        ResponseType.NotLinked -> "this server is not linked to a Discord server"
                        ResponseType.RateLimit -> "you've sent too many messages in a very short amount of time"
                        ResponseType.NotAcceptable -> "there is either no Discord message channel set or it was unexpectedly modified"
                        else -> "[unknown reason]. Please report this to the developers"
                    }}."
                )
            } else {
                sender.sendMessage("§aYour message was sent to the Discord server.")
                Bukkit.broadcast("$author -> Discord: $message", "discordmc.message.read")
            }
        }
        return true
    }

    fun linkInfo(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty())
            return false

        Requests.async({ getInfo() }) {
            sender.sendMessage(
                when (type) {
                    ResponseType.OK -> """
                    |§aThis server is linked to §6${content!!.guild.name} (${content.guild.id})§a.
                    |${if (content.linkedChannel == null) "§cNo message channel is set"
                    else "Messages are being forwarded to §6# ${content.linkedChannel}"}
                    """.trimMargin()
                    ResponseType.NotLinked -> "§aThis server is currently not linked to any Discord server."
                    ResponseType.RateLimit -> "§cCould not get info - you are sending too many requests at once."
                    else -> "§cUnknown issue. Please report this to the developers."
                }
            )
        }
        return true
    }

}