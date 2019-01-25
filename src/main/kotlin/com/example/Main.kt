package com.example

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.*
import io.ktor.routing.*

fun Application.main() {
    routing {
        get("/") {
            call.respondText("Hello world", ContentType.Text.Html)
        }

        static("static") {
            resources("html")
        }
    }
}
