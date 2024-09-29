package com.mehmet

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.serialization.gson.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import com.mongodb.client.model.Filters.eq
import java.nio.charset.StandardCharsets

val userBloomFilter: BloomFilter<String> = BloomFilter.create(
    Funnels.stringFunnel(StandardCharsets.UTF_8),
    1000,
    0.01
)

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }
        install(CallLogging)
        install(Authentication) {
            jwt("auth-jwt") {
                verifier(JwtConfig.verifier)
                validate { credential ->
                    if (credential.payload.getClaim("username").asString().isNotEmpty())
                        JWTPrincipal(credential.payload)
                    else
                        null
                }
            }
        }
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    val client = HttpClient()
    val serversConfig = loadServersConfig("src/main/resources/servers.yaml") // servers.yaml path gradlew komutu ile çalıştırırken
//application.kt ile çalıştırırken
  //  val serversConfig = loadServersConfig("C:\\Users\\mkarad\\Desktop\\New folder (4)\\Tour_Agency_Api-main\\Tour_Agency_Api-main\\Tour_Agency_Api_Project-main\\kotlin-backend\\ktor-tour-agency-api\\src\\main\\resources\\servers.yaml") // servers.yaml path

    routing {
        get("/") {
            call.respondText("Hello, Ktor!")
        }

        route("/register") {
            post {
                val postParameters = call.receiveParameters()
                val username = postParameters["username"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing username")
                val password = postParameters["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")
//TO DO:
                // Search for the user by username in the database
                val existingUser = userCollection.find(eq("username", username)).first()

                // If the user exists, throw an error
                if (existingUser != null) {
                    return@post call.respond(HttpStatusCode.Conflict, "Username already exists (Database)")
                }

                val result = withContext(Dispatchers.IO) { registerUser(username, password) }
                if (result.isSuccess) {
                    userBloomFilter.put(username)
                    call.respond(HttpStatusCode.Created, "User registered successfully")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "User registration failed")
                }
            }
        }

        route("/login") {
            post {
                val postParameters = call.receiveParameters()
                val username = postParameters["username"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing username")
                val password = postParameters["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")

                if (!userBloomFilter.mightContain(username)) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@post
                }

                val isAuthenticated = withContext(Dispatchers.IO) { authenticateUser(username, password) }
                if (isAuthenticated) {
                    val token = JwtConfig.makeToken(username)
                    call.respond(HttpStatusCode.OK, mapOf("token" to token))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                }
            }
        }

        authenticate("auth-jwt") {
            route("/fetch") {
                get {
                    val url = call.request.queryParameters["url"]
                    if (url.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Missing URL parameter")
                        return@get
                    }

                    val server = serversConfig.servers.find { it.url == url }
                    if (server == null) {
                        call.respond(HttpStatusCode.NotFound, "Server not found")
                        return@get
                    }

                    try {
                        val response = fetchFromServer(client, server)
                        call.respondText(response, ContentType.Application.Json)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                    }
                }
            }
            route("/fetch-all") {
                get {
                    val responses = fetchFromAllServersAsJson(client, serversConfig.servers)
                    call.respondText(responses)
                }
            }
        }
    }
}

