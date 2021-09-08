package sh.rebecca.inventory.definition

import com.displee.cache.CacheLibrary
import image.Graphics2D
import image.Graphics3D
import image.Sprite
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import media.Model
import org.apache.commons.collections4.map.LRUMap
import org.springframework.stereotype.Component
import sh.rebecca.inventory.model.ModelService
import sh.rebecca.inventory.util.readString317


data class Obj(
    var id: Int,
    var model: Int,
    var originalColors: IntArray? = null,
    var replacementColors: IntArray? = null,
    var pitch: Int,
    var yaw: Int,
    var roll: Int,
    var translateX: Int,
    var translateY: Int,
    var zoom: Int,
    var ambient: Int,
    var attenuation: Int,
    var scaleX: Int,
    var scaleY: Int,
    var scaleZ: Int)


@Component
class ObjReader(private val library: CacheLibrary, private val modelService: ModelService) {

    var offsets: IntArray? = null
    val objData = library.data(0, 2, "obj.dat")
    val objIndex = library.data(0, 2, "obj.idx")
    var count = 0
    var sprites: LRUMap<Int, Sprite> = LRUMap(100)

    init {
        val idxBuffer = Unpooled.wrappedBuffer(objIndex)
        count = idxBuffer.readUnsignedShort()
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
        return decode(dataBuffer, id)
    }

    fun decode(buffer: ByteBuf, id: Int): Obj {
        var modelId = 0
        var zoom = 0
        var roll = 0
        var translateX = 0
        var translateY = 0
        var pitch = 0
        var yaw = 0
        var originalColors: IntArray? = null
        var replacementColors: IntArray? = null
        var ambient = 0
        var attenuation = 0
        var scaleX = 0
        var scaleY = 0
        var scaleZ = 0

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

    fun sprite(id: Int, stackSize: Int, backColour: Int): Sprite? {

        var stackSize = stackSize
        if (backColour == 0) {
            var sprite: Sprite? = sprites[id]

            if (sprite != null) {
                return sprite
            }
        }
        var definition: Obj = lookup(id)


        val model: Model = modelService.getModel(definition.model) ?: return null

        Graphics3D.createPalette(0.7);
        if ((definition.scaleX != 128) || (definition.scaleZ != 128) || (definition.scaleY != 128)) {
            //model.scale(definition.scaleX, definition.scaleY, definition.scaleZ)
        }
        if (definition.originalColors != null) {
            for (i in 0 until definition.originalColors!!.size) {
                model.recolor(definition.originalColors!![i], definition.replacementColors!![i])
            }
        }
        model.calculateBoundaries();
        model.calculateNormals();
        model.calculateLighting(64 + definition.ambient, 768 + definition.attenuation, -50, -10, -50);


        val rendered = Sprite(32, 32)
        val centreX: Int = Graphics3D.centerX
        val centreY: Int = Graphics3D.centerY
        val scanOffsets: IntArray = Graphics3D.offsets
        val raster: IntArray = Graphics2D.target
        val width: Int = Graphics2D.targetWidth
        val height: Int = Graphics2D.targetHeight
        val clipLeft: Int = Graphics2D.left
        val clipRight: Int = Graphics2D.right
        val clipBottom: Int = Graphics2D.bottom
        val clipTop: Int = Graphics2D.top

        Graphics3D.texturedShading = true

        Graphics2D.setTarget(rendered.pixels, 32, 32)
        Graphics2D.fillRect(0, 0, 32, 32, 0)
        Graphics3D.setOffsets()
        var scale: Int = definition.zoom
        if (backColour == -1) {
            scale = (scale * 1.5).toInt()
        }
        if (backColour > 0) {
            scale = (scale * 1.04).toInt()
        }
        val sin: Int = (Graphics3D.sin[definition.pitch] * definition.zoom) shr 16
        val cos: Int = (Graphics3D.cos[definition.pitch] * definition.zoom) shr 16
        model?.draw(0, definition.yaw, definition.roll, definition.pitch, definition.translateX, sin + (model.minBoundY / 2) + definition.translateY, cos + definition.translateY)
        for (x in 31 downTo 0) {
            for (y in 31 downTo 0) {
                if (rendered.pixels[x + y * 32] == 0) {
                    if (x > 0 && rendered.pixels[x - 1 + y * 32] > 1) {
                        rendered.pixels[x + y * 32] = 1
                    } else if (y > 0 && rendered.pixels[x + (y - 1) * 32] > 1) {
                        rendered.pixels[x + y * 32] = 1
                    } else if (x < 31 && rendered.pixels[x + 1 + y * 32] > 1) {
                        rendered.pixels[x + y * 32] = 1
                    } else if (y < 31 && rendered.pixels[x + (y + 1) * 32] > 1) {
                        rendered.pixels[x + y * 32] = 1
                    }
                }
            }
        }
      if (backColour == 0) {
            for (x in 31 downTo 0) {
                for (y in 31 downTo 0) {
                    if (rendered.pixels[x + y * 32] == 0 && x > 0 && y > 0 &&rendered.pixels[x - 1 + (y - 1) * 32] > 0) {
                        rendered.pixels[x + y * 32] = 0x302020
                    }
                }
            }
        }

        if (backColour == 0) {
            sprites[id] = rendered
        }
        Graphics2D.setTarget(raster, width, height)
        Graphics2D.setBounds(clipLeft, clipTop, clipRight, clipBottom)
        Graphics3D.centerX = centreX
        Graphics3D.centerY = centreY
        Graphics3D.offsets = scanOffsets

        return rendered
    }


}
