package config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class User(
    val email: String,
    val name: String
)

@Serializable
data class Core(
    val bare: Boolean
)

@Serializable
data class Config(
    val core: Core,
    val user: User
) {
    fun createConfigFile(kgitDir: String): Result<Unit> {
        val configFile = File(kgitDir, CONFIG_FILE_NAME)
        return try {
            val jsonString = Json.encodeToString(Config.serializer(), this)
            configFile.writeText(jsonString)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private const val CONFIG_FILE_NAME = "config"

fun readConfigFile(kgitDir: String): Result<Config> {
    val configFile = File(kgitDir, CONFIG_FILE_NAME)
    return try {
        if (!configFile.exists()) {
            return Result.failure(FileNotFoundException("config.Config file not found at ${configFile.absolutePath}"))
        }
        val jsonString = configFile.readText()
        val config = Json.decodeFromString(Config.serializer(), jsonString)
        Result.success(config)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class FileNotFoundException(message: String) : Exception(message)
