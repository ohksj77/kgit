import command.CommandFactory

const val KGIT_DIR = ".kgit"

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("사용법: kgit <명령어> [옵션]")
        return
    }
    val command = CommandFactory.getCommand(args[0], KGIT_DIR)
    command.execute(args)
}
