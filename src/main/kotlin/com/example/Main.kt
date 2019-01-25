package com.example

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receiveParameters
import io.ktor.response.*
import io.ktor.routing.*

val users = mutableListOf<String>()

fun Application.main() {
    routing {
        get("/") {
            call.respondText("Hello world", ContentType.Text.Html)
        }

        post("/newUser") {
            val post = call.receiveParameters()
            val userName = post["userName"]
            if (userName != null) {
                users.add(userName)
            }

            call.respondText("List of created users: $users", ContentType.Text.Html)
        }

        static("static") {
            resources("html")
        }
    }
}
