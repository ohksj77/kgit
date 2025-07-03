package command

enum class CommandType() {
    INIT,
    ADD,
    LS,
    CAT,
    WRITE,
    COMMIT,
    TAG;

    companion object {
        fun from(commandName: String): CommandType =
            entries.firstOrNull { it.name.lowercase() == commandName }
                ?: throw IllegalArgumentException("지원하지 않는 명령입니다: $commandName")
    }
}
