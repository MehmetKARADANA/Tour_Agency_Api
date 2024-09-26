package com.mehmet

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay


@Serializable
data class Server(val name: String, val url: String, val distance_km: Int)

@Serializable
data class ServersConfig(val servers: List<Server>)

// Function that reads the YAML file and returns a ServersConfig object
fun loadServersConfig(path: String): ServersConfig {
    val yaml = Yaml()
    val fileContent = File(path).readText()
    val config = yaml.load<Map<String, List<Map<String, Any>>>>(fileContent)
    val servers = config["servers"]!!.map { server ->
        Server(
            name = server["name"] as String,
            url = server["url"] as String,
            distance_km = server["distance_km"] as Int
        )
    }
    return ServersConfig(servers)
}


suspend fun fetchFromServer(client: HttpClient, server: Server): String {
    return try {
        val delayMillis = server.distance_km / 10
        delay(delayMillis.toLong())
        println("Delaying request to ${server.url} for $delayMillis ms")

        val response = client.get("${server.url}/rooms")
        response.bodyAsText()
    } catch (e: Exception) {
        "Error fetching data from ${server.url}"
    }

}
suspend fun fetchFromAllServersAsJson(client: HttpClient, servers: List<Server>): String = coroutineScope {
    val responses = servers.map { server ->
        async {
            val delayMillis = server.distance_km / 10
            delay(delayMillis.toLong())

            val response = fetchFromServer(client, server)
            if (response.isNotEmpty()) {
                response
            } else {
                null
            }
        }
    }.awaitAll()

    val nonEmptyResponses = responses.filterNotNull()
    if (nonEmptyResponses.isEmpty()) {
        "[]" // If all responses are empty, return an empty JSON array
    } else {
        nonEmptyResponses.joinToString(separator = ", ", prefix = "[", postfix = "]")
    }
}