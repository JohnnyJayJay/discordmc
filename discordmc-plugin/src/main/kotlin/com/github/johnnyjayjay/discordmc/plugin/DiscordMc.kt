package com.github.johnnyjayjay.discordmc.plugin

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class DiscordMc: JavaPlugin() {

    override fun onEnable() {
        getCommand("register")?.setExecutor(Commands::register)
        getCommand("linkinfo")?.setExecutor(Commands::linkInfo)
        getCommand("detach")?.setExecutor(Commands::detach)
        getCommand("discord")?.setExecutor(Commands::discord)
        TokenStash.applyToken()
        Bukkit.getLogger().log(Level.INFO, "Components loaded - plugin created by Johnny#3826")
        if (Requests.token == null)
            Bukkit.getLogger().log(Level.INFO, "This server is not linked yet - see here how to do it: ")
    }

}