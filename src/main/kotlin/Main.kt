import config.Config
import config.Core
import config.User
import `object`.CatFileOperationType
import `object`.CatFileParam
import `object`.CommitTreeParam
import `object`.LSTreeOption
import `object`.LSTreeParam
import `object`.UpdateIndexParam
import `object`.WriteTreeParam
import `object`.lsTree
import `object`.writeTree
import porcelain.TagParam
import porcelain.tag
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("사용법: kgit <명령어> [옵션]")
        return
    }
    val kgitDir = ".kgit"
    when (args[0]) {
        "init" -> {
            val dir = File(kgitDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            File(dir, "objects").mkdirs()
            File(dir, "refs").mkdirs()
            val user = User(email = "user@example.com", name = "user")
            val config = Config(core = Core(bare = false), user = user)
            val result = config.createConfigFile(kgitDir)
            println(if (result.isSuccess) "저장소 초기화 완료" else "초기화 실패: ${result.exceptionOrNull()?.message}")
        }

        "add" -> {
            if (args.size < 2) {
                println("사용법: kgit add <파일명>")
                return
            }
            val file = args[1]
            val updateIndexParam = UpdateIndexParam(
                files = listOf(file),
                caches = listOf(),
                kgitDir = kgitDir,
                add = true
            )
            val result = updateIndexParam.updateIndex()
            println(if (result.isSuccess) "add 성공" else "add 실패: ${result.exceptionOrNull()?.message}")
        }

        "ls" -> {
            if (args.size < 2) {
                println("사용법: kgit ls-tree <트리해시>")
                return
            }
            val treeHash = args[1]
            val param = LSTreeParam(
                kgitDir = kgitDir,
                objectHash = treeHash,
                option = LSTreeOption(recursive = false, tree = true)
            )
            val result = lsTree(param)
            if (result.isSuccess) {
                result.getOrNull()?.forEach { println(it) }
            } else {
                println("ls-tree 실패: ${result.exceptionOrNull()?.message}")
            }
        }

        "cat" -> {
            if (args.size < 3) {
                println("사용법: kgit cat-file <type|pretty-print|size|exist> <오브젝트해시>")
                return
            }
            val opType = when (args[1]) {
                "type" -> CatFileOperationType.TYPE
                "pretty-print" -> CatFileOperationType.PRETTY_PRINT
                "size" -> CatFileOperationType.SIZE
                "exist" -> CatFileOperationType.EXIST
                else -> {
                    println("지원하지 않는 cat-file 타입")
                    return
                }
            }
            val objectHash = args[2]
            val param = CatFileParam(
                kgitDir = kgitDir,
                operationType = opType,
                objectHash = objectHash
            )
            val result = param.catFile()
            println(result.getOrElse { "cat-file 실패: ${it.message}" })
        }

        "write" -> {
            val param = WriteTreeParam(kgitDir = kgitDir)
            val result = writeTree(param)
            println(result.getOrElse { "write-tree 실패: ${it.message}" })
        }

        "commit" -> {
            if (args.size < 3) {
                println("사용법: kgit commit-tree <트리해시> <커밋메시지>")
                return
            }
            val treeHash = args[1]
            val message = args[2]
            val param = CommitTreeParam(
                kgitDir = kgitDir,
                objectHash = treeHash,
                parentCommitHash = listOf(),
                message = message,
                author = "user <user@example.com>"
            )
            val result = param.commitTree()
            println(result.getOrElse { "commit-tree 실패: ${it.message}" })
        }

        "tag" -> {
            if (args.size < 3) {
                println("사용법: kgit tag <태그명> <타겟해시> [메시지]")
                return
            }
            val tagName = args[1]
            val target = args[2]
            val message = if (args.size > 3) args[3] else ""
            val user = User(email = "user@example.com", name = "user")
            val param = TagParam(
                user = user,
                kgitDir = kgitDir,
                tagName = tagName,
                target = target,
                message = message,
                delete = false
            )
            val result = tag(param)
            if (result.isSuccess) {
                println("tag 성공")
            } else {
                println("tag 실패: ${result.exceptionOrNull()}")
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        else -> {
            println("지원하지 않는 명령입니다: ${args[0]}")
        }
    }
}
