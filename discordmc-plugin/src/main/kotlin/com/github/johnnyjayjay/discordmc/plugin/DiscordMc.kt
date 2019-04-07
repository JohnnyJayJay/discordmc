package com.github.johnnyjayjay.discordmc.plugin

import org.bukkit.plugin.java.JavaPlugin

object DiscordMc: JavaPlugin() {

    override fun onEnable() {
        getCommand("register")?.setExecutor(Commands::register)
        getCommand("linkinfo")?.setExecutor(Commands::linkInfo)
        getCommand("detach")?.setExecutor(Commands::detach)
        getCommand("discord")?.setExecutor(Commands::discord)
        TokenStash.applyToken()
    }

}