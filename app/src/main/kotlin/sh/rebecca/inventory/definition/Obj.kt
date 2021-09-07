package sh.rebecca.inventory.definition

import com.displee.cache.CacheLibrary
import io.guthix.buffer.readString0CP1252
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import media.Model
import org.springframework.stereotype.Component
import sh.rebecca.inventory.util.readString317


data class Obj(
    var model: Int,
    var originalColors: IntArray? = null,
    var replacementColors: IntArray? = null,
    var pitch: Int,
    var yaw: Int,
    var translateX: Int,
    var translateY: Int,
    var zoom: Int)





@Component
class ObjReader(private val library: CacheLibrary) {

    var offsets: IntArray? = null
    val objData = library.data(0, 2, "obj.dat")
    val objIndex = library.data(0, 2, "obj.idx")

     init {
        val idxBuffer = Unpooled.wrappedBuffer(objIndex)
        val count = idxBuffer.readUnsignedShort()
        offsets = IntArray(count)
        var offset = 2
        for(i in 0 until count) {
            offsets!![i] = offset
            offset += idxBuffer.readUnsignedShort()
        }
    }

    fun lookup(id: Int): Obj {
        val dataBuffer = Unpooled.wrappedBuffer(objData)
        dataBuffer.readBytes(offsets!![id])
        return decode(dataBuffer)
    }

    fun decode(buffer: ByteBuf): Obj {
        var modelId = 0
        var zoom = 0
        var translateX = 0
        var translateY = 0
        var pitch = 0
        var yaw = 0
        var originalColors: IntArray? = null
        var replacementColors: IntArray? = null

        do {
            var opcode = buffer.readUnsignedByte()
            when(opcode.toInt()) {
                1 ->  modelId = buffer.readUnsignedShort()
                2 -> buffer.readString317()
                3 -> buffer.readString317()
                4 -> zoom = buffer.readUnsignedShort()
                5 -> pitch = buffer.readUnsignedShort()
                6 -> buffer.readUnsignedShort()
                7 -> {
                    translateX = buffer.readUnsignedShort()
                    if(translateX > 32767) {
                        translateX -= 0x10000
                    }
                }
                8 -> {
                    translateY = buffer.readUnsignedShort()
                    if(translateY > 32767) {
                        translateY -= 0x10000
                    }
                }
                10 -> buffer.readUnsignedShort()
                12 -> buffer.readInt()
                23 -> {
                    buffer.readUnsignedShort()
                    buffer.readByte()
                }
                24 -> buffer.readUnsignedShort()
                25 -> {
                    buffer.readUnsignedShort()
                    buffer.readByte()
                }
                26 -> buffer.readUnsignedShort()
                in 30..34 -> {
                    buffer.readString317()
                }
                in 35..39 -> {
                    buffer.readString317()
                }
                40 -> {
                    var count = buffer.readUnsignedByte()
                    originalColors = IntArray(count.toInt())
                    replacementColors = IntArray(count.toInt())
                    for(i in 0 until count) {
                        originalColors[i] = buffer.readUnsignedShort()
                        replacementColors[i] = buffer.readUnsignedShort()
                    }
                }
                78 -> buffer.readUnsignedShort()
                79 -> buffer.readUnsignedShort()
                90 -> buffer.readUnsignedShort()
                91 -> buffer.readUnsignedShort()
                92 -> buffer.readUnsignedShort()
                93 -> buffer.readUnsignedShort()
                95 -> yaw = buffer.readUnsignedShort()
                97 -> buffer.readUnsignedShort()
                98 -> buffer.readUnsignedShort()
                in 100..109 -> {
                    buffer.readUnsignedShort()
                    buffer.readUnsignedShort()
                }
                110 -> buffer.readUnsignedShort()
                111 -> buffer.readUnsignedShort()
                112 -> buffer.readUnsignedShort()
                113 -> buffer.readByte()
                114 -> buffer.readByte() * 5
                115 -> buffer.readUnsignedShort()
                0 -> return Obj(modelId, originalColors, replacementColors, pitch, yaw, translateX, translateY, zoom)
            }
        } while (true)
    }

}
