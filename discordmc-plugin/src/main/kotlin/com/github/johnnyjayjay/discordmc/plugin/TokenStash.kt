package com.github.johnnyjayjay.discordmc.plugin

import java.io.File

object TokenStash {

    private val file = File("./internal/token.txt").apply {
        if (!this.exists()) {
            val parent = this.parentFile
            if (!parent.exists())
                parent.mkdirs()
            this.createNewFile()
        }
    }

    var token: String?
        get() {
            val content = file.readText()
            return if (content.isEmpty()) null else content
        }
        set(value) = file.writeText(value ?: "")

    fun applyToken() {
        Requests.token = token
    }
}