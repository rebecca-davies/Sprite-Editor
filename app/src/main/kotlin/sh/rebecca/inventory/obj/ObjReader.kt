package sh.rebecca.inventory.obj

import io.Buffer
import org.springframework.stereotype.Component

@Component
class ObjReader {

    fun read(buffer: Buffer, id: Int): Obj {
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
        var name = ""

        do {
            val opcode = buffer.read()
            when(opcode) {
                1 ->  modelId = buffer.readUShort()
                2 -> name = buffer.readString()
                3 -> buffer.readStringBytes()
                4 -> zoom = buffer.readUShort()
                5 -> pitch = buffer.readUShort()
                6 -> yaw = buffer.readUShort()
                7 -> {
                    translateX = buffer.readUShort()
                    if(translateX > 32767) {
                        translateX -= 65536
                    }
                }
                8 -> {
                    translateY = buffer.readUShort()
                    if(translateY > 32767) {
                        translateY -= 65536
                    }
                }
                10 -> buffer.readUShort()
                12 -> buffer.readInt()
                23 -> {
                    buffer.readUShort()
                    buffer.readByte()
                }
                24 -> buffer.readUShort()
                25 -> {
                    buffer.readUShort()
                    buffer.readByte()
                }
                26 -> buffer.readUShort()
                in 30..34 -> {
                    buffer.readString()
                }
                in 35..39 -> {
                    buffer.readString()
                }
                40 -> {
                    val count = buffer.read()
                    originalColors = IntArray(count)
                    replacementColors = IntArray(count)
                    for(i in 0 until count) {
                        originalColors[i] = buffer.readUShort()
                        replacementColors[i] = buffer.readUShort()
                    }
                }
                78 -> buffer.readUShort()
                79 -> buffer.readUShort()
                90 -> buffer.readUShort()
                91 -> buffer.readUShort()
                92 -> buffer.readUShort()
                93 -> buffer.readUShort()
                95 -> roll = buffer.readUShort()
                97 -> buffer.readUShort()
                98 -> buffer.readUShort()
                in 100..109 -> {
                    buffer.readUShort()
                    buffer.readUShort()
                }
                110 -> scaleX = buffer.readUShort()
                111 -> scaleZ = buffer.readUShort()
                112 -> scaleY = buffer.readUShort()
                113 -> ambient = buffer.readByte().toInt()
                114 -> attenuation = buffer.readByte() * 5
                115 -> buffer.read()
                0 -> return Obj(id, name, modelId, originalColors, replacementColors, pitch, yaw, roll, translateX, translateY, zoom, ambient, attenuation, scaleX, scaleY, scaleZ)
            }
        } while (true)
    }
}
