package `object`

object GitMode {
    // 디렉토리 (Tree)
    const val TREE_MODE: Long = 0b010000000000000000000L // 0o040000

    // 일반 파일 (Blob)
    const val BLOB_MODE: Long = 0b00110001001010010101010101010101L // 0o100644

    // 실행 가능한 일반 파일 (Blob)
    const val EXECUTABLE_BLOB_MODE: Long = 0b00110001111011010101101101101L // 0o100755

    // 심볼릭 링크 (Blob)
    const val SYMLINK_MODE: Long = 0b101000000000000000000L // 0o120000

    // 서브모듈 (Commit)
    const val SUBMODULE_MODE: Long = 0b111000000000000000000L // 0o160000
}
