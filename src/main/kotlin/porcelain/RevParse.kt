package porcelain

import reference.SymbolicRefParam
import reference.SymbolicRefType
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readBytes

data class RevParseParam(
    val kgitDir: String,
    val target: String
) {
    fun revParse(): Result<String> {
        val kgitDirFile = File(kgitDir)

        val symbolicRefPath = File(kgitDirFile, target).toPath()
        if (symbolicRefPath.exists()) {
            val refResult =
                SymbolicRefParam(
                    kgitDir = kgitDir,
                    type = SymbolicRefType.fromString(target) ?: SymbolicRefType.HEAD,
                    referencePath = "",
                    delete = false
                ).symbolicRef()

            val ref = refResult.getOrElse { return Result.failure(it) }

            return RevParseParam(
                kgitDir = kgitDir,
                target = ref
            ).revParse()
        }

        var foundHash: String? = null
        val refsDir = File(kgitDirFile, "refs").toPath()

        if (refsDir.exists() && refsDir.isDirectory()) {
            try {
                Files.walk(refsDir)
                    .filter { it.exists() && !it.isDirectory() }
                    .forEach { path ->
                        val fileName = path.fileName.toString()
                        if (fileName == target || path.toString().endsWith(target)) {
                            foundHash = String(path.readBytes()).trim()
                            throw BreakWalkException()
                        }
                    }
            } catch (e: BreakWalkException) {
                if (foundHash != null && foundHash.isNotEmpty()) {
                    return Result.success(foundHash)
                }
                return Result.failure(e)
            } catch (e: IOException) {
                return Result.failure(e)
            }
        }

        if (foundHash != null && foundHash.isNotEmpty()) {
            return Result.success(foundHash)
        }

        if (target.length < 4) {
            return Result.failure(IllegalArgumentException("ambiguous argument '${target}': unknown revision or path not in the working tree"))
        }

        val objDirPrefix = target.substring(0, 2)
        val objNameSuffix = target.substring(2)
        val objectsDir = File(kgitDirFile, "objects").toPath()
        val specificObjDir = File(objectsDir.toFile(), objDirPrefix).toPath()

        if (specificObjDir.exists() && specificObjDir.isDirectory()) {
            try {
                Files.walk(specificObjDir)
                    .filter { it.exists() && !it.isDirectory() }
                    .forEach { path ->
                        val fileName = path.fileName.toString()
                        if (fileName.startsWith(objNameSuffix)) {
                            foundHash = objDirPrefix + fileName
                            throw BreakWalkException()
                        }
                    }
            } catch (e: BreakWalkException) {
                if (foundHash != null && foundHash.isNotEmpty()) {
                    return Result.success(foundHash)
                }
                return Result.failure(e)
            } catch (e: IOException) {
                return Result.failure(e)
            }
        }
        if (foundHash != null && foundHash.isNotEmpty()) {
            return Result.success(foundHash)
        }

        return Result.failure(IllegalArgumentException("ambiguous argument '${target}': unknown revision or path not in the working tree"))
    }
}

private class BreakWalkException : RuntimeException()
