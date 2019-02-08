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
import org.omg.CORBA.DynAnyPackage.Invalid
import java.net.URL

const val signInPath = "/signIn"
const val signUpPath = "/signUp"

const val signInFormActionPath = "/signIn"
const val signUpFormActionPath = "/signUp"

val users = mutableMapOf<Int, User>()

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
            val res: URL? = this::class.java.getResource("/html/index.html")
            val map = mapOf("signInPath" to signInPath)
            val text: String = res!!.readText().replaceTexts(map)
            call.respondText(text, ContentType.Text.Html)
        }

        get(signInPath) {
            val res: URL? = this::class.java.getResource("/html/signIn.html")
            val map = mapOf("signInFormActionPath" to signInPath, "signUpPath" to signUpPath)
            val text: String = res!!.readText().replaceTexts(map)
            call.respondText(text, ContentType.Text.Html)
        }

        post(signUpFormActionPath) {
            val post = call.receiveParameters()
            val userName = post["userName"]
            val password = post["password"]
            val passwordConfirmation = post["passwordConfirmation"]
            if (userName != null && password != null && password == passwordConfirmation) {
                val maxId = users.keys.max() ?: 0
                val newId = maxId + 1
                users[newId] = User(userName, password)

                call.respondText("List of created users: $users", ContentType.Text.Html)
            }
            else {
                call.respond(TextContent("Bad request", ContentType.Text.Html, HttpStatusCode.BadRequest))
            }
        }

        get(signUpPath) {
            val res: URL? = this::class.java.getResource("/html/signUp.html")
            val map = mapOf("newUserPath" to signUpFormActionPath)
            val text: String = res!!.readText().replaceTexts(map)
            call.respondText(text, ContentType.Text.Html)
        }

        static("css") {
            resources("css")
        }
    }
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)