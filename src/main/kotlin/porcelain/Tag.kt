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
) {
    fun tag(): Result<Unit> {
        val referencePath = File("refs", "tags").resolve(tagName).path
        if (delete) {
            return UpdateRefParam(
                kgitDir = kgitDir,
                referencePath = referencePath,
                delete = true
            ).updateRef()
        }

        if (target.isEmpty()) {
            return Result.success(Unit)
        }

        val objHashResult = RevParseParam(
            kgitDir = kgitDir,
            target = target
        ).revParse()
        val objHash = objHashResult.getOrElse { return Result.failure(it) }

        if (message.isEmpty()) {
            return UpdateRefParam(
                kgitDir = kgitDir,
                referencePath = referencePath,
                objectHash = objHash,
                delete = false
            ).updateRef()
        }

        val typeResult =
            CatFileParam(
                kgitDir = kgitDir,
                operationType = CatFileOperationType.TYPE,
                objectHash = objHash
            ).catFile()

        val objTypeString = typeResult.getOrElse { return Result.failure(it) }

        val objType = Type.fromString(objTypeString)
            ?: return Result.failure(IllegalArgumentException("Unknown object type: $objTypeString"))

        val tagHashResult = MKTagParam(
            kgitDir = kgitDir,
            objectType = objType,
            objectHash = objHash,
            name = tagName,
            message = message,
            tagger = user.email
        ).mkTag()

        val tagHash = tagHashResult.getOrElse { return Result.failure(it) }

        return UpdateRefParam(
            kgitDir = kgitDir,
            referencePath = referencePath,
            objectHash = tagHash,
            delete = false
        ).updateRef()
    }
}
