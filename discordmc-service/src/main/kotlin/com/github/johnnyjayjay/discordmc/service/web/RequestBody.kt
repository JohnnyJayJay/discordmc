package com.github.johnnyjayjay.discordmc.service.web

import java.util.*

data class PostMessageBody(val content: String, val author: String)

data class RegisterBody(val guid: UUID, val code: Int)