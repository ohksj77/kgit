package `object`

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

const val INDEX_FILE_NAME = "index"

@Serializable
data class IndexEntry(
    val treeEntry: TreeEntry
) {
    val file: String
        get() = treeEntry.file
}

typealias Index = MutableList<IndexEntry>

fun openIndex(kgitDir: String): Result<Index> {
    val indexPath = File(kgitDir, INDEX_FILE_NAME)

    if (!indexPath.exists()) {
        return Result.success(mutableListOf())
    }

    return runCatching {
        val data = indexPath.readText()
        Json.decodeFromString<Index>(data)
    }
}

fun writeIndex(kgitDir: String, index: Index): Result<Unit> {
    index.sortBy { it.file }

    return runCatching {
        val data = Json.encodeToString(index)
        val indexPath = File(kgitDir, INDEX_FILE_NAME)
        indexPath.parentFile.mkdirs()
        indexPath.writeText(data)
    }
}

fun searchIndex(index: Index, file: String): Pair<Int, IndexEntry?> {
    val foundIndex = index.binarySearch { it.file.compareTo(file) }

    return if (foundIndex >= 0) {
        Pair(foundIndex, index[foundIndex])
    } else {
        Pair(0, null)
    }
}
