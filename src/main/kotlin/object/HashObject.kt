package `object`

import java.io.File

data class HashObjectParam(
    val kgitDir: String,
    val type: Type,
    val data: ByteArray,
    var dryRun: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) {
            return false
        }

        other as HashObjectParam

        if (dryRun != other.dryRun) {
            return false
        }
        if (kgitDir != other.kgitDir) {
            return false
        }
        if (type != other.type) {
            return false
        }
        if (!data.contentEquals(other.data)) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = dryRun.hashCode()
        result = 31 * result + kgitDir.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    fun hashObject(): Result<String> {
        val key = newKey(type, data)

        if (dryRun) {
            return Result.success(key)
        }

        val compressedContentResult = zlibCompress(newContent(type, data))
        val compressedContent = compressedContentResult.getOrElse { return Result.failure(it) }

        val objectDir = File(key.dir(kgitDir))
        val objectFile = File(key.path(kgitDir))

        return try {
            objectDir.mkdirs()
            objectFile.writeBytes(compressedContent)
            Result.success(key)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
