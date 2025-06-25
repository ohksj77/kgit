package `object`

enum class CatFileOperationType(val value: String) {
    PRETTY_PRINT("pretty-print"),
    TYPE("type"),
    SIZE("size"),
    EXIST("exist");

    override fun toString(): String = value
}

data class CatFileParam(
    val kgitDir: String,
    val operationType: CatFileOperationType,
    val objectHash: String
) {
    fun catFile(): Result<String> {
        val objResult = parseObject(kgitDir, objectHash)

        val obj = objResult.getOrElse {
            return Result.failure(it)
        }

        return when (operationType) {
            CatFileOperationType.PRETTY_PRINT -> Result.success(String(obj.data))
            CatFileOperationType.TYPE -> Result.success(obj.type.value)
            CatFileOperationType.SIZE -> Result.success(obj.length.toString())
            CatFileOperationType.EXIST -> Result.success("")
        }
    }
}
