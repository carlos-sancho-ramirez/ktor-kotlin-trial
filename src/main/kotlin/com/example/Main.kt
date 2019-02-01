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

const val newUserPath = "/newUserX"

val users = mutableListOf<String>()

fun replaceTexts(inStream: InputStream, outStream: OutputStream, map: Map<String, String>) {
    do {
        var byte = inStream.read()
        if (byte >= 0) {
            if (byte == '$'.toInt()) {
                byte = inStream.read()
                if (byte >= 0) {
                    if (byte == '{'.toInt()) {
                        byte = inStream.read()
                        var key = ""
                        while (byte >= 0 && byte != '}'.toInt()) {
                            key += byte.toChar()
                            byte = inStream.read()
                        }

                        val text = if (byte == '}'.toInt()) map.get(key) ?: "\${$key}" else "\${$key"
                        for (ch: Char in text) {
                            outStream.write(ch.toInt())
                        }
                    }
                    else {
                        outStream.write('$'.toInt())
                        outStream.write(byte)
                    }
                }
                else {
                    outStream.write('$'.toInt())
                }
            }
            else {
                outStream.write(byte)
            }
        }
    } while (byte >= 0)
}

fun Application.main() {
    install(StatusPages) {
        status(HttpStatusCode.NotFound) {
            call.respond(TextContent("${it.value} ${it.description}", ContentType.Text.Plain.withCharset(Charsets.UTF_8), it))
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
            call.respondOutputStream(ContentType.Text.Html, HttpStatusCode.OK) {
                val inStream = this::class.java.getResourceAsStream("/html/newUser.html")
                val map = mapOf("newUserPath" to newUserPath)
                replaceTexts(inStream, this, map)
            }
        }

        static("css") {
            resources("css")
        }
    }
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)