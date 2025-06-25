package reference

import java.io.File

data class UpdateRefParam(
    val kgitDir: String,
    val referencePath: String,
    val objectHash: String = "",
    val delete: Boolean
) {
    fun updateRef(): Result<Unit> {
        val refFile = File(kgitDir, referencePath)

        return try {
            refFile.parentFile.mkdirs()

            if (!delete) {
                refFile.writeText(objectHash)
                Result.success(Unit)
            } else {
                val deleted = refFile.delete()
                if (deleted || !refFile.exists()) {
                    Result.success(Unit)
                } else {
                    Result.failure(RuntimeException("Failed to delete file: ${refFile.absolutePath}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
