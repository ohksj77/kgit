package `object`

import kotlinx.serialization.Serializable

@Serializable
data class TreeEntry(
    val mode: Long = 0L,
    val file: String,
    val objectHash: String = ""
) {
    fun isDirectory(): Boolean {
        return mode == GitMode.TREE_MODE
    }

    fun isSymlink(): Boolean {
        return mode == GitMode.SYMLINK_MODE
    }

    fun isExecutable(): Boolean {
        return mode == GitMode.EXECUTABLE_BLOB_MODE
    }

    fun isRegularFile(): Boolean {
        return !isDirectory() && !isSymlink() && mode != GitMode.SUBMODULE_MODE
    }
}

typealias TreeEntries = List<TreeEntry>
