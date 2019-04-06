package com.github.johnnyjayjay.discordmc.service.web

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.github.johnnyjayjay.discordmc.service.Server
import com.github.johnnyjayjay.discordmc.service.Servers
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.select

object JwtAuth {

    private val secret = System.getenv("discordmc_secret")
    private const val issuer = "discordmc"
    private val algorithm = Algorithm.HMAC512(secret)

    private val verifier: JWTVerifier = JWT.require(algorithm).withIssuer(
        issuer
    ).build()

    fun createToken(): String = JWT.create()
        .withIssuer(issuer)
        .withSubject("Authentication")
        .sign(algorithm)

    fun Authentication.Configuration.configure() {
        jwt {
            verifier(verifier)
            realm = "discordmc"
            validate {
                val id = it.payload.id
                Servers.select {
                    Servers.authId eq id
                }.map(Server.Companion::fromResultRow).firstOrNull()
            }
        }
    }
}

object Verification {

    private val digits = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    private val codesWaiting = mutableMapOf<Int, Long>()

    fun exists(code: Int) = code in codesWaiting

    fun associatedGuild(code: Int) = codesWaiting[code]!!

    fun insert(guildId: Long): Int {
        val code = generateCode()
        codesWaiting[code] = guildId
        GlobalScope.launch {
            delay(600_000)
            codesWaiting.remove(code)
        }
        return code
    }

    private fun generateCode(): Int {
        val codeBuilder = StringBuilder()
        for (i in 0..5)
            codeBuilder.append(digits.random())
        val code = codeBuilder.toString().toInt()
        return if (code in codesWaiting) generateCode() else code
    }

}

