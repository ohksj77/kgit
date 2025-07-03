package `object`

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

data class WriteTreeParam(
    val kgitDir: String
) {
    fun writeTree(): Result<String> {
        val indexResult = openIndex(kgitDir)
        val index = indexResult.getOrElse { return Result.failure(it) }

        val treeResult = buildTree(index)
        val tree = treeResult.getOrElse { return Result.failure(it) }

        return createTreeObject(kgitDir, tree)
    }
}

fun createTreeObject(kgitDir: String, tree: Map<String, Any>): Result<String> {
    val workingCopy = File(".").absolutePath
        ?: return Result.failure(IllegalStateException("kgitDir does not have a parent directory"))

    val entries: MutableList<TreeEntry> = mutableListOf()

    val sortedKeys = tree.keys.sorted()

    for (key in sortedKeys) {
        val value = tree[key] ?: continue

        if (value is Map<*, *>) {
            val subTree = value as Map<String, Any>
            val hashResult = createTreeObject(kgitDir, subTree)
            val hash = hashResult.getOrElse { return Result.failure(it) }

            val filePath = File(workingCopy, key)
            if (!filePath.isDirectory) {
                return Result.failure(IllegalArgumentException("$key is not a directory in working copy but treated as a subtree"))
            }

            val mode = GitMode.TREE_MODE

            entries.add(
                TreeEntry(
                    mode = mode,
                    file = key,
                    objectHash = hash
                )
            )
            continue
        }

        if (value is TreeEntry) {
            val entry = value
            val filePath = File(workingCopy, entry.file)

            if (!filePath.exists()) {
                return Result.failure(FileNotFoundException("${entry.file}: file not found"))
            }

            val fileDataResult = try {
                Result.success(Files.readAllBytes(filePath.toPath()))
            } catch (e: Exception) {
                return Result.failure(e)
            }
            val fileData = fileDataResult.getOrElse { return Result.failure(it) }

            val hashResult = HashObjectParam(
                dryRun = false,
                kgitDir = kgitDir,
                type = Type.BLOB,
                data = fileData
            ).hashObject()

            val hash = hashResult.getOrElse { return Result.failure(it) }

            val gitMode = when {
                Files.isDirectory(filePath.toPath()) -> GitMode.TREE_MODE
                Files.isSymbolicLink(filePath.toPath()) -> GitMode.SYMLINK_MODE
                else -> {
                    val perms = Files.getPosixFilePermissions(filePath.toPath())
                    if (perms.contains(PosixFilePermission.OWNER_EXECUTE) ||
                        perms.contains(PosixFilePermission.GROUP_EXECUTE) ||
                        perms.contains(PosixFilePermission.OTHERS_EXECUTE)
                    ) {
                        GitMode.EXECUTABLE_BLOB_MODE
                    } else {
                        GitMode.BLOB_MODE
                    }
                }
            }

            entries.add(
                TreeEntry(
                    mode = gitMode,
                    file = entry.file,
                    objectHash = hash
                )
            )
            continue
        }

        return Result.failure(IllegalArgumentException("Invalid value type in tree map: $value"))
    }

    entries.sortBy { it.file }

    val dataResult = try {
        Result.success(Json.encodeToString(entries).toByteArray())
    } catch (e: Exception) {
        Result.failure(e)
    }
    val data = dataResult.getOrElse { return Result.failure(it) }

    return HashObjectParam(
        dryRun = false,
        kgitDir = kgitDir,
        type = Type.TREE,
        data = data
    ).hashObject()
}

fun buildTree(index: Index): Result<Map<String, Any>> {
    val tree: MutableMap<String, Any> = mutableMapOf()

    for (trackingFile in index) {
        var currentParent: MutableMap<String, Any> = tree
        var currentParentPath = ""
        val paths = trackingFile.file.split(File.separatorChar)

        for (i in 0 until paths.size - 1) {
            val dirName = paths[i]
            val fullPathForDir = if (currentParentPath.isEmpty()) dirName else File(currentParentPath, dirName).path

            @Suppress("UNCHECKED_CAST")
            var dirMap = currentParent[fullPathForDir] as? MutableMap<String, Any>
            if (dirMap == null) {
                dirMap = mutableMapOf()
                currentParent[fullPathForDir] = dirMap
            }
            currentParent = dirMap
            currentParentPath = fullPathForDir
        }

        currentParent[trackingFile.file] = TreeEntry(
            file = trackingFile.file,
        )
    }

    return Result.success(tree)
}
