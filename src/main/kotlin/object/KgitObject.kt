package `object`

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

const val DIR_NAME = "objects"

enum class Type(val value: String) {
    BLOB("blob"),
    TREE("tree"),
    COMMIT("commit"),
    TAG("tag");

    override fun toString(): String = value

    companion object {
        fun fromString(typeString: String): Type? {
            return Type.entries.firstOrNull { it.value == typeString }
        }
    }
}

fun String.dir(kgitDir: String): String {
    require(this.length >= 2) { "Object key must be at least 2 characters long" }
    return File(kgitDir, DIR_NAME).resolve(this.substring(0, 2)).path
}

fun String.path(kgitDir: String): String {
    require(this.length >= 2) { "Object key must be at least 2 characters long" }
    return File(this.dir(kgitDir)).resolve(this.substring(2)).path
}

fun newKey(typeVal: Type, data: ByteArray): String {
    val str = newContent(typeVal, data)
    val digest = MessageDigest.getInstance("SHA-1")
    digest.update(str)
    return digest.digest().joinToString("") { "%02x".format(it) }
}

fun newContent(typeVal: Type, data: ByteArray): ByteArray {
    val header = "${typeVal.value} ${data.size}\u0000".toByteArray()
    val output = ByteArrayOutputStream()
    output.write(header)
    output.write(data)
    return output.toByteArray()
}

fun zlibCompress(data: ByteArray): Result<ByteArray> {
    return try {
        val bos = ByteArrayOutputStream()
        val dos = DeflaterOutputStream(bos)
        dos.write(data)
        dos.close()
        Result.success(bos.toByteArray())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

data class KgitObject(
    val type: Type,
    val length: Int,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KgitObject

        if (type != other.type) return false
        if (length != other.length) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + length
        result = 31 * result + data.contentHashCode()
        return result
    }
}

fun parseObject(kgitDir: String, objectHash: String): Result<KgitObject> {
    val path = objectHash.path(kgitDir)
    val file = File(path)

    if (!file.exists()) {
        return Result.failure(FileNotFoundException("Object file not found at $path"))
    }

    return try {
        val data = file.readBytes()
        unmarshalObject(data)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun unmarshalObject(data: ByteArray): Result<KgitObject> {
    return try {
        val bis = ByteArrayInputStream(data)
        val iis = InflaterInputStream(bis)
        val decompressedData = iis.readBytes()

        val nullByteIndex = decompressedData.indexOf('\u0000'.code.toByte())
        if (nullByteIndex == -1) {
            return Result.failure(IllegalArgumentException("Invalid object format: null byte separator not found"))
        }

        val headerBytes = decompressedData.sliceArray(0 until nullByteIndex)
        val bodyBytes = decompressedData.sliceArray(nullByteIndex + 1 until decompressedData.size)

        val headerString = String(headerBytes)
        val parts = headerString.split(" ")

        if (parts.size != 2) {
            return Result.failure(IllegalArgumentException("Invalid object header format: expected 'type length'"))
        }

        val objectType = Type.fromString(parts[0])
            ?: return Result.failure(IllegalArgumentException("Invalid object type: ${parts[0]}"))
        val objectLength = parts[1].toInt()

        if (objectLength != bodyBytes.size) {
            return Result.failure(IllegalArgumentException("Mismatched object length: header states $objectLength, actual data is ${bodyBytes.size}"))
        }
        Result.success(KgitObject(objectType, objectLength, bodyBytes))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
