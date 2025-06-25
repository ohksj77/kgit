package `object`

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LSTreeOption(
    val recursive: Boolean,
    val tree: Boolean
)

data class LSTreeParam(
    val kgitDir: String,
    val objectHash: String,
    val option: LSTreeOption
)

fun lsTree(param: LSTreeParam): Result<TreeEntries> {
    val objResult = parseObject(param.kgitDir, param.objectHash)

    val obj = objResult.getOrElse {
        return Result.failure(it)
    }

    if (obj.type != Type.TREE) {
        return Result.failure(IllegalArgumentException("${param.objectHash} is invalid tree hash"))
    }

    val entriesResult = try {
        Result.success(Json.decodeFromString<TreeEntries>(String(obj.data)))
    } catch (e: Exception) {
        Result.failure(e)
    }

    val entries = entriesResult.getOrElse {
        return Result.failure(it)
    }

    val ret: MutableList<TreeEntry> = mutableListOf()
    for (e in entries) {
        if (!e.isDirectory()) {
            ret.add(e)
            continue
        }

        if (param.option.tree) {
            ret.add(e)
        }

        if (!param.option.recursive) {
            continue
        }

        val subEntriesResult = lsTree(
            LSTreeParam(
                kgitDir = param.kgitDir,
                objectHash = e.objectHash,
                option = param.option
            )
        )

        val subEntries = subEntriesResult.getOrElse {
            return Result.failure(it)
        }

        ret.addAll(subEntries)
    }
    ret.sortBy { it.file }

    return Result.success(ret)
}
