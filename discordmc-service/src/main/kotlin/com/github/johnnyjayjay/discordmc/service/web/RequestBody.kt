package com.github.johnnyjayjay.discordmc.service.web

data class PostMessageBody(val content: String, val author: String)

data class RegisterBody(val serverId: String, val code: Int)