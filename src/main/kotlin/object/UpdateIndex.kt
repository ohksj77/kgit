package `object`

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

data class UpdateIndexParam(
    val files: List<String>,
    val caches: List<TreeEntry>,
    val kgitDir: String,
    val add: Boolean
) {
    fun updateIndex(): Result<Unit> {
        val indexResult = openIndex(kgitDir)
        val index = indexResult.getOrElse { return Result.failure(it) }

        if (caches.isNotEmpty()) {
            for (cache in caches) {
                val (entryIndex, entry) = searchIndex(index, cache.file)
                if (entry != null) {
                    index[entryIndex] = IndexEntry(treeEntry = cache)
                    continue
                }

                if (!add) {
                    return Result.failure(IllegalArgumentException("${cache.file}: cannot add to the index"))
                }

                index.add(IndexEntry(treeEntry = cache))
            }

            return writeIndex(kgitDir, index)
        }

        for (file in files) {
            val workingCopyDir = File(kgitDir).parentFile
            val filePath = File(workingCopyDir, file)

            if (!filePath.exists()) {
                return Result.failure(FileNotFoundException("${file}: file not found"))
            }

            if (filePath.isDirectory) {
                return Result.failure(IllegalArgumentException("${file}: is a directory"))
            }

            val dataResult = try {
                Result.success(filePath.readBytes())
            } catch (e: Exception) {
                return Result.failure(e)
            }

            val data = dataResult.getOrElse { return Result.failure(it) }

            val hashResult = HashObjectParam(
                dryRun = false,
                kgitDir = kgitDir,
                type = Type.BLOB,
                data = data
            ).hashObject()

            val hash = hashResult.getOrElse { return Result.failure(it) }

            val gitMode = when {
                filePath.isDirectory -> GitMode.TREE_MODE
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

            val (entryindex, entry) = searchIndex(index, file)
            if (entry != null) {
                index[entryindex] = IndexEntry(
                    treeEntry = TreeEntry(
                        mode = gitMode,
                        file = file,
                        objectHash = hash
                    )
                )
                continue
            }

            if (!add) {
                return Result.failure(IllegalArgumentException("${file}: cannot add to the index"))
            }

            index.add(
                IndexEntry(
                    treeEntry = TreeEntry(
                        mode = gitMode,
                        file = file,
                        objectHash = hash
                    )
                )
            )
        }

        return writeIndex(kgitDir, index)
    }
}
