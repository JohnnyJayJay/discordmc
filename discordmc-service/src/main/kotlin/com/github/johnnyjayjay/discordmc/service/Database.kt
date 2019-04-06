package com.github.johnnyjayjay.discordmc.service

import io.ktor.auth.Principal
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import java.util.*

object Servers : Table() {
    val guildId = long("guild_id").uniqueIndex()
    val guid = uuid("guid").uniqueIndex()
    val authId = varchar("auth_id", 60).primaryKey()
    val channelId = long("message_channel_id").default(0)
}

data class Server(val guildId: Long, val guid: UUID, val authId: String, val channelId: Long) : Principal {
    companion object {
        fun fromResultRow(row: ResultRow) =
            Server(row[Servers.guildId], row[Servers.guid], row[Servers.authId], row[Servers.channelId])

        fun exists(guildId: Long) =
            !Servers.select {
                Servers.guildId eq guildId
            }.empty()

        fun exists(authId: String) =
            !Servers.select {
                Servers.authId eq authId
            }.empty()

    }
}