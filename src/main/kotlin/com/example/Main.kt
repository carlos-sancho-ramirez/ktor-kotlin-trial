package com.example

import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.application.*
import io.ktor.features.StatusPages
import io.ktor.http.*
import io.ktor.http.content.TextContent
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.mustache.Mustache
import io.ktor.mustache.MustacheContent
import io.ktor.request.receiveParameters
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import org.omg.CORBA.DynAnyPackage.Invalid
import java.net.URL

const val signInPath = "/signIn"
const val signUpPath = "/signUp"

const val signInFormActionPath = "/signIn"
const val signUpFormActionPath = "/signUp"

var sessionCounter = 0
val users = mutableMapOf<Int, User>()
val sessions = mutableMapOf<Int, Int>() // Maps session id with its user id

fun Routing.getMustache(path: String, htmlFile: String, replacements: Map<String, String>): Route {
    return get(path) {
        var session: Session? = call.sessions.get()
        if (session == null) {
            session = Session(++sessionCounter)
            call.sessions.set(session)
        }

        var repl = replacements.toMutableMap()
        val userId = sessions[session.id]
        if (userId != null) {
            repl["userAlias"] = users[userId]!!.name
        }

        call.respond(MustacheContent("$htmlFile.html", repl))
    }
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

    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates/html")
    }

    install(Sessions) {
        cookie<Session>("DEVICE_ID")
    }

    routing {
        getMustache("/", "index", mapOf("signInPath" to signInPath))
        getMustache(signInPath, "signIn", mapOf("signInFormActionPath" to signInPath, "signUpPath" to signUpPath))
        getMustache(signUpPath, "signUp", mapOf("newUserPath" to signUpFormActionPath))

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

        post(signInFormActionPath) {
            val post = call.receiveParameters()
            val userName = post["userName"]
            val password = post["password"]
            val userIds = if (userName != null && password != null) users.filterValues { user -> user.name == userName && user.password == password }.keys else setOf()
            val session = call.sessions.get<Session>()
            if (userIds.size == 1 && session != null) {
                sessions[session.id] = userIds.first()
                call.respondText("Signed in as: $userName", ContentType.Text.Html)
            }
            else {
                call.respond(TextContent("Bad request", ContentType.Text.Html, HttpStatusCode.BadRequest))
            }
        }

        static("css") {
            resources("css")
        }
    }
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)