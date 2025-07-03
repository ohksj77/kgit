package command

import java.util.*

object CommandFactory {
    private val commandMap: EnumMap<CommandType, (String) -> Command> = EnumMap(CommandType::class.java)

    init {
        commandMap[CommandType.INIT] = { kgitDir -> InitCommand(kgitDir) }
        commandMap[CommandType.ADD] = { kgitDir -> AddCommand(kgitDir) }
        commandMap[CommandType.LS] = { kgitDir -> LsCommand(kgitDir) }
        commandMap[CommandType.CAT] = { kgitDir -> CatCommand(kgitDir) }
        commandMap[CommandType.WRITE] = { kgitDir -> WriteCommand(kgitDir) }
        commandMap[CommandType.COMMIT] = { kgitDir -> CommitCommand(kgitDir) }
        commandMap[CommandType.TAG] = { kgitDir -> TagCommand(kgitDir) }
    }

    fun getCommand(commandName: String, kgitDir: String): Command {
        val type = CommandType.from(commandName)
        return commandMap[type]?.invoke(kgitDir)
            ?: throw IllegalArgumentException("지원하지 않는 명령입니다: $commandName")
    }
}
