package com.github.johnnyjayjay.discordmc.plugin

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

object Commands {

    fun register(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val code = if (args.size == 1) args[0].toIntOrNull() ?: return false else return false
        @Suppress("DEPRECATION")
        val serverId = sender.server.serverId
        Requests.async {
            val result = register(serverId, code)
            if (result == null) {
                sender.sendMessage(
                    "§cThat is not a valid verification code. Make sure " +
                            "that you entered everything correctly and that the code has not expired already."
                )
            } else {
                Requests.token = result.first
                TokenStash.token = result.first
                val guild = result.second
                sender.sendMessage("§aSuccessfully connected this server with §6${guild.name} (${guild.id})§a!")
            }
        }
        return true
    }

    fun detach(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty())
            return false

        Requests.async {
            if (detach()) {
                sender.sendMessage("§aThe link to the Discord server was successfully deleted.")
            } else {
                sender.sendMessage("§cThere is no link to detach from!")
            }
        }
        return true
    }

    fun discord(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty())
            return false

        val message = args.joinToString(" ")
        val author = sender.name
        Requests.async {
            if (postMessage(author = author, content = message)) {
                sender.sendMessage("§aYour message was sent to the Discord server.")
                Bukkit.broadcast("$author -> Discord: $message", "discordmc.message.read")
            } else {
                sender.sendMessage(
                    "§cMessage to Discord server could not be sent. Make sure that a Discord server " +
                            "and a corresponding text channel are linked to this server by using §a/linkinfo§c."
                )
            }
        }
        return true
    }

    fun linkInfo(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty())
            return false

        val info = Requests.getInfo()
        sender.sendMessage(
            if (info == null) "§aThis server is currently not linked to any Discord server"
            else "§aThis server is linked to §6${info.guild.name} (${info.guild.id})§a.\n" +
                    if (info.linkedChannel == null) "No message channel is set."
                    else "Messages are being forwarded to §6# ${info.linkedChannel}§a."
        )
        return true
    }

}