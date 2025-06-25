package `object`

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

@Serializable
data class CommitObjectBody(
    val tree: String,
    val parent: List<String>,
    val author: String,
    val date: String,
    val message: String
)

data class CommitTreeParam(
    val kgitDir: String,
    val objectHash: String,
    val parentCommitHash: List<String>,
    val message: String,
    val author: String
) {
    fun commitTree(): Result<String> {
        val objResult = parseObject(kgitDir, objectHash)

        val obj = objResult.getOrElse {
            return Result.failure(it)
        }

        if (obj.type != Type.TREE) {
            return Result.failure(IllegalArgumentException("$objectHash is not a valid 'tree' object"))
        }

        val body = CommitObjectBody(
            tree = objectHash,
            parent = parentCommitHash,
            author = author,
            date = LocalDateTime.now().toString(),
            message = message
        )

        val dataResult = try {
            Result.success(Json.encodeToString(body).toByteArray())
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val data = dataResult.getOrElse { return Result.failure(it) }

        return HashObjectParam(
            dryRun = false,
            kgitDir = kgitDir,
            type = Type.COMMIT,
            data = data
        ).hashObject()
    }
}
