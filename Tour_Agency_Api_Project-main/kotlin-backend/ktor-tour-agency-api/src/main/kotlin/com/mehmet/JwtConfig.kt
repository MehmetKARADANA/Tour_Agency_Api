package com.mehmet

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

object JwtConfig {
    private const val secret = "my_secret_key"
    val verifier = JWT.require(Algorithm.HMAC256(secret)).build()

    fun makeToken(username: String): String = JWT.create()
        .withClaim("username", username)
        .sign(Algorithm.HMAC256(secret))
}
