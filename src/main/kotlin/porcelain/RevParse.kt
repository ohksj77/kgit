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
)

fun revParse(param: RevParseParam): Result<String> {
    val kgitDirFile = File(param.kgitDir)

    val symbolicRefPath = File(kgitDirFile, param.target).toPath()
    if (symbolicRefPath.exists()) {
        val refResult =
            SymbolicRefParam(
                kgitDir = param.kgitDir,
                type = SymbolicRefType.fromString(param.target) ?: SymbolicRefType.HEAD,
                referencePath = "",
                delete = false
            ).symbolicRef()

        val ref = refResult.getOrElse { return Result.failure(it) }

        return revParse(
            RevParseParam(
                kgitDir = param.kgitDir,
                target = ref
            )
        )
    }

    var foundHash: String? = null
    val refsDir = File(kgitDirFile, "refs").toPath()

    if (refsDir.exists() && refsDir.isDirectory()) {
        try {
            Files.walk(refsDir)
                .filter { it.exists() && !it.isDirectory() }
                .forEach { path ->
                    val fileName = path.fileName.toString()
                    if (fileName == param.target || path.toString().endsWith(param.target)) {
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

    if (param.target.length < 4) {
        return Result.failure(IllegalArgumentException("ambiguous argument '${param.target}': unknown revision or path not in the working tree"))
    }

    val objDirPrefix = param.target.substring(0, 2)
    val objNameSuffix = param.target.substring(2)
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

    return Result.failure(IllegalArgumentException("ambiguous argument '${param.target}': unknown revision or path not in the working tree"))
}

private class BreakWalkException : RuntimeException()
