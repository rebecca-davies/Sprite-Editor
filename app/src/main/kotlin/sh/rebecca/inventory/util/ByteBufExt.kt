package sh.rebecca.inventory.util

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets

private const val stringTerminator = 10.toByte()

fun ByteBuf.readString317(): String {
    val bytes = mutableListOf<Byte>()
    var current: Byte
    while (true) {
        current = this.readByte()
        if (current == stringTerminator) {

            break
        }
        bytes.add(current)
    }
    return String(bytes.toByteArray())
}

fun ByteBuf.writeString317(text: String) {
    val bytes = "$text\n".toByteArray(StandardCharsets.US_ASCII)
    this.writeBytes(bytes)
}
