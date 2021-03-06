package com.github.johnnyjayjay.discordmc.service

import com.beust.klaxon.Klaxon
import com.github.johnnyjayjay.discordmc.service.web.JwtAuth.configure
import com.github.johnnyjayjay.discordmc.service.bot.Bot
import com.github.johnnyjayjay.discordmc.service.ratelimit.MapRateLimitController
import com.github.johnnyjayjay.discordmc.service.ratelimit.RateLimits
import com.github.johnnyjayjay.discordmc.service.ratelimit.rateLimit
import com.github.johnnyjayjay.discordmc.service.web.Endpoints
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils

val klaxon = Klaxon()

@KtorExperimentalAPI
fun main(args: Array<String>) {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/discordmc",
        driver = "org.postgresql.Driver",
        user = "application",
        password = System.getenv("discordmc_dbpw")
    )

    Bot.start()
    val server = embeddedServer(factory = Netty, port = 8080) {
        install(Authentication)
        install(RateLimits)

        authentication {
            configure()
        }

        routing {

            post("/register") { Endpoints.register(this) }

            authenticate {
                rateLimit(limit = 5, seconds = 5) {
                    get("/info") { Endpoints.linkInfo(this) }
                    post("/message") { Endpoints.postMessage(this) }
                }
                delete("/detach") { Endpoints.detach(this) }
            }
        }
    }

    server.start()

}

