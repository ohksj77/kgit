package reference

import java.io.File

enum class SymbolicRefType {
    HEAD,
    FETCH_HEAD,
    ORIG_HEAD,
    MERGE_HEAD;

    companion object {
        fun fromString(typeString: String): SymbolicRefType? {
            return entries.firstOrNull { it.name == typeString }
        }
    }
}

private val predefinedTypes = setOf(
    SymbolicRefType.HEAD,
    SymbolicRefType.FETCH_HEAD,
    SymbolicRefType.ORIG_HEAD,
    SymbolicRefType.MERGE_HEAD
)

data class SymbolicRefParam(
    val kgitDir: String,
    val type: SymbolicRefType,
    val referencePath: String,
    val delete: Boolean
) {
    fun symbolicRef(): Result<String> {
        val path = File(kgitDir, type.name)

        if (delete) {
            if (predefinedTypes.contains(type)) {
                return Result.failure(IllegalArgumentException("${type}: refusing to delete predefined symbolic ref"))
            }
            return try {
                if (path.exists()) {
                    path.deleteRecursively()
                }
                Result.success("")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        if (referencePath.isEmpty()) {
            return path.readSymbolicRef()
        }

        if (predefinedTypes.contains(type) && !referencePath.startsWith("refs/")) {
            return Result.failure(IllegalArgumentException("${referencePath}: refusing to point $type outside of refs/"))
        }

        return try {
            path.parentFile.mkdirs()
            path.writeText(referencePath.symbolicRefFormat())
            Result.success("")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun File.readSymbolicRef(): Result<String> {
    return try {
        if (!exists()) {
            return Result.failure(java.io.FileNotFoundException("File not found: $absolutePath"))
        }
        val data = readText()
        Result.success(data.removePrefix("ref: "))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun String.symbolicRefFormat(): String {
    return "ref: $this"
}
