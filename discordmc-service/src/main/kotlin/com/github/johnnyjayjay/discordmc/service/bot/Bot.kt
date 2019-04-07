package com.github.johnnyjayjay.discordmc.service.bot

import com.jagrosh.jdautilities.command.CommandClientBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder

object Bot {

    const val prefix = "_"

    private var jda: JDA? = null
    private val token = System.getenv("discordmc_token")

    fun start() {
        val client = CommandClientBuilder()
            .setPrefix(prefix)
            .setOwnerId("234343108773412864")
            .addAnnotatedModule(Commands)
            .build()
        jda = JDABuilder(token).addEventListener(client, DeletionListener).build()
    }

    fun getGuildName(id: Long) = jda!!.getGuildById(id)?.name

    fun getChannel(guildId: Long, channelId: Long) =
        jda!!.getGuildById(guildId)?.getTextChannelById(channelId)
}