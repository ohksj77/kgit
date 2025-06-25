package porcelain

import config.User
import `object`.CatFileOperationType
import `object`.CatFileParam
import `object`.MKTagParam
import `object`.Type
import reference.UpdateRefParam
import java.io.File

data class TagParam(
    val user: User,
    val kgitDir: String,
    val tagName: String,
    val target: String,
    val message: String,
    val delete: Boolean
)

fun tag(param: TagParam): Result<Unit> {
    val referencePath = File("refs", "tags").resolve(param.tagName).path
    if (param.delete) {
        return UpdateRefParam(
            kgitDir = param.kgitDir,
            referencePath = referencePath,
            delete = true
        ).updateRef()
    }

    if (param.target.isEmpty()) {
        return Result.success(Unit)
    }

    val objHashResult = revParse(
        RevParseParam(
            kgitDir = param.kgitDir,
            target = param.target
        )
    )
    val objHash = objHashResult.getOrElse { return Result.failure(it) }

    if (param.message.isEmpty()) {
        return UpdateRefParam(
            kgitDir = param.kgitDir,
            referencePath = referencePath,
            objectHash = objHash,
            delete = false
        ).updateRef()
    }

    val typeResult =
        CatFileParam(
            kgitDir = param.kgitDir,
            operationType = CatFileOperationType.TYPE,
            objectHash = objHash
        ).catFile()

    val objTypeString = typeResult.getOrElse { return Result.failure(it) }

    val objType = Type.fromString(objTypeString)
        ?: return Result.failure(IllegalArgumentException("Unknown object type: $objTypeString"))

    val tagHashResult = MKTagParam(
        kgitDir = param.kgitDir,
        objectType = objType,
        objectHash = objHash,
        name = param.tagName,
        message = param.message,
        tagger = param.user.email
    ).mkTag()

    val tagHash = tagHashResult.getOrElse { return Result.failure(it) }

    return UpdateRefParam(
        kgitDir = param.kgitDir,
        referencePath = referencePath,
        objectHash = tagHash,
        delete = false
    ).updateRef()
}
