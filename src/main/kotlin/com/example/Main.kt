package com.example

import io.ktor.application.*
import io.ktor.features.StatusPages
import io.ktor.http.*
import io.ktor.http.content.TextContent
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receiveParameters
import io.ktor.response.*
import io.ktor.routing.*
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

const val newUserPath = "/newUserX"

val users = mutableListOf<String>()

fun String.replaceTexts(map: Map<String, String>): String {
    var start = 0
    val sb = StringBuilder()
    do {
        val end = indexOf("\${", start)
        if (end >= 0) {
            sb.append(substring(start, end))
            val tokenEnd = indexOf("}", end + 2)
            if (tokenEnd < end + 2) {
                throw IllegalArgumentException("Unclosed token")
            }

            val token = substring(end + 2, tokenEnd)
            if (!map.containsKey(token)) {
                throw IllegalArgumentException("No key provided to replace token $token")
            }

            sb.append(map[token])
            start = tokenEnd + 1
        }
    } while (end >= 0)

    sb.append(substring(start))
    return sb.toString()
}

fun Application.main() {
    install(StatusPages) {
        status(HttpStatusCode.NotFound) {
            call.respond(TextContent("${it.value} ${it.description}", ContentType.Text.Plain.withCharset(Charsets.UTF_8), it))
        }
        exception<Throwable> { cause ->
            call.respond(TextContent("500 Internal Server Error. ${cause::class.java.simpleName}. ${cause.message}",
                    ContentType.Text.Plain.withCharset(Charsets.UTF_8),
                    HttpStatusCode.InternalServerError))
        }
    }

    routing {
        get("/") {
            call.respondText("Hello world", ContentType.Text.Html)
        }

        post(newUserPath) {
            val post = call.receiveParameters()
            val userName = post["userName"]
            if (userName != null) {
                users.add(userName)
            }

            call.respondText("List of created users: $users", ContentType.Text.Html)
        }

        get("/newUserForm") {
            val res: URL? = this::class.java.getResource("/html/newUser.html")
            val map = mapOf("newUserPath" to newUserPath)
            val text: String = res!!.readText().replaceTexts(map)
            call.respondText(text, ContentType.Text.Html)
        }

        static("css") {
            resources("css")
        }
    }
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)