package command

enum class CommandType(val commandName: String) {
    INIT("init"),
    ADD("add"),
    LS("ls"),
    CAT("cat"),
    WRITE("write"),
    COMMIT("commit"),
    TAG("tag");

    companion object {
        fun from(commandName: String): CommandType =
            entries.firstOrNull { it.commandName == commandName }
                ?: throw IllegalArgumentException("지원하지 않는 명령입니다: $commandName")
    }
}
