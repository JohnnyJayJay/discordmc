package com.github.johnnyjayjay.discordmc.service.bot

import com.github.johnnyjayjay.discordmc.service.Servers
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.deleteWhere

object DeletionListener: ListenerAdapter() {

    override fun onGuildLeave(event: GuildLeaveEvent) {
        Servers.deleteWhere {
            Servers.guildId eq event.guild.idLong
        }
    }
}