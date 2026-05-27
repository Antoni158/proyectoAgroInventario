package com.example.inventario.security

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {

    private const val PREFIX = "sha256:"

    /** TEMPORAL — true solo para pruebas locales. Volver a false en producción. */
    const val PLAINTEXT_DEV_MODE = true

    fun hash(password: String): String {
        if (PLAINTEXT_DEV_MODE) return password
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = digest(password, salt)
        return PREFIX + salt.toHex() + ":" + hash.toHex()
    }

    fun verify(password: String, stored: String): Boolean {
        if (stored.isBlank()) return false
        if (PLAINTEXT_DEV_MODE) return stored == password
        if (!stored.startsWith(PREFIX)) {
            return stored == password
        }
        val parts = stored.removePrefix(PREFIX).split(":")
        if (parts.size != 2) return false
        val salt = parts[0].hexToBytes() ?: return false
        val expected = parts[1].hexToBytes() ?: return false
        val actual = digest(password, salt)
        return actual.contentEquals(expected)
    }

    fun needsRehash(stored: String): Boolean {
        if (PLAINTEXT_DEV_MODE) return false
        return !stored.startsWith(PREFIX)
    }

    private fun digest(password: String, salt: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        md.update(password.toByteArray(Charsets.UTF_8))
        return md.digest()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray? = try {
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    } catch (_: Exception) {
        null
    }
}
