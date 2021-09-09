package sh.rebecca.inventory.obj

import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Component
import sh.rebecca.inventory.buffer.readString317

@Component
class ObjReader {

    fun read(buffer: ByteBuf, id: Int): Obj {
        var modelId = 0
        var zoom = 2000
        var translateX = 0
        var translateY = 0
        var pitch = 0
        var yaw = 0
        var roll = 0
        var originalColors: IntArray? = null
        var replacementColors: IntArray? = null
        var ambient = 0
        var attenuation = 0
        var scaleX = 128
        var scaleY = 128
        var scaleZ = 128

        do {
            var opcode = buffer.readUnsignedByte()
            when(opcode.toInt()) {
                1 ->  modelId = buffer.readUnsignedShort()
                2 -> buffer.readString317()
                3 -> buffer.readString317()
                4 -> zoom = buffer.readUnsignedShort()
                5 -> pitch = buffer.readUnsignedShort()
                6 -> yaw = buffer.readUnsignedShort()
                7 -> {
                    translateX = buffer.readUnsignedShort()
                    if(translateX > 32767) {
                        translateX -= 65536
                    }
                }
                8 -> {
                    translateY = buffer.readUnsignedShort()
                    if(translateY > 32767) {
                        translateY -= 65536
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
                95 -> roll = buffer.readUnsignedShort()
                97 -> buffer.readUnsignedShort()
                98 -> buffer.readUnsignedShort()
                in 100..109 -> {
                    buffer.readUnsignedShort()
                    buffer.readUnsignedShort()
                }
                110 -> scaleX = buffer.readUnsignedShort()
                111 -> scaleZ = buffer.readUnsignedShort()
                112 -> scaleY = buffer.readUnsignedShort()
                113 -> ambient = buffer.readByte().toInt()
                114 -> attenuation = buffer.readByte() * 5
                115 -> buffer.readUnsignedShort()
                0 -> return Obj(id, modelId, originalColors, replacementColors, pitch, yaw, roll, translateX, translateY, zoom, ambient, attenuation, scaleX, scaleY, scaleZ)
            }
        } while (true)
    }
}
