package com.mehmet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.eq
import java.security.MessageDigest

// User veri sınıfı
data class User(val username: String, val password: String)

private const val MONGO_URI = "mongodb+srv://mehmet34:mehmet175e@atlascluster.j3z8vqq.mongodb.net/mydatabase?retryWrites=true&w=majority"
val client = KMongo.createClient(MONGO_URI).coroutine
val database = client.getDatabase("mydatabase")
val userCollection = database.getCollection<User>()

suspend fun registerUser(username: String, password: String): Result<User> {
    return withContext(Dispatchers.IO) {
        runCatching {
            val user = User(username, hashPassword(password))
            userCollection.insertOne(user)
            user
        }
    }
}

suspend fun authenticateUser(username: String, password: String): Boolean {
    val user = withContext(Dispatchers.IO) {
        userCollection.findOne(User::username eq username)
    }
    return user != null && user.password == hashPassword(password)
}

fun hashPassword(password: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
    return hash.joinToString("") { "%02x".format(it) }
}
