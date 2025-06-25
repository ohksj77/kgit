package `object`

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MKTagParam(
    val kgitDir: String,
    val objectType: Type,
    val objectHash: String,
    val name: String,
    val message: String,
    val tagger: String
) {
    fun mkTag(): Result<String> {
        val body = TagBody(
            `object` = objectHash,
            type = objectType,
            tag = name,
            tagger = tagger
        )

        val dataResult = try {
            Result.success(Json.encodeToString(TagBody.serializer(), body).toByteArray())
        } catch (e: Exception) {
            Result.failure(e)
        }

        val data = dataResult.getOrElse { return Result.failure(it) }

        return HashObjectParam(
            kgitDir = kgitDir,
            type = Type.TAG,
            data = data
        ).hashObject()
    }
}

@Serializable
data class TagBody(
    val `object`: String,
    val type: Type,
    val tag: String,
    val tagger: String
)
