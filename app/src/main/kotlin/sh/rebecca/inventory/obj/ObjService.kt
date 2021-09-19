package sh.rebecca.inventory.obj

import image.Graphics2D
import image.Graphics3D
import image.Sprite
import media.Model
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import sh.rebecca.inventory.model.ModelService

@Service
class ObjService(private val repository: ObjRepository, private val modelService: ModelService) {

    fun getCount(): Int {
        return repository.getCount()
    }

    fun getObj(id: Int): Obj? {
        return repository.findById(id)
    }

   @Cacheable("objsprite")
    fun getObjSprite(obj: Obj): Sprite? {
        val model = modelService.getModel(obj.model) ?: return null
        Graphics3D.createPalette(0.7)

        if ((obj.scaleX != 128) || (obj.scaleZ != 128) || (obj.scaleY != 128)) {
            model.scale(obj.scaleX, obj.scaleZ, obj.scaleY);
        }

        if (obj.originalColors != null) {
            for (i in 0 until obj.originalColors!!.size) {
                model.recolor(obj.originalColors!![i], obj.replacementColors!![i])
            }
        }
        model.applyLighting(64 + obj.ambient, 768 + obj.attenuation, -50, -10, -50, true)

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

        val sinPitch: Int = (Graphics3D.sin[obj.pitch] * obj.zoom) shr 16
        val cosPitch: Int = (Graphics3D.cos[obj.pitch] * obj.zoom) shr 16
        model.drawSimple(0, obj.yaw ,obj.roll, obj.pitch, obj.translateX, sinPitch + (model.minBoundY / 2) + obj.translateY, cosPitch + obj.translateY)

        for (x in 31 downTo 0) {
            for (y in 31 downTo 0) {
                if (rendered.pixels[x + (y * 32)] == 0) {
                    if (x > 0 && rendered.pixels[(x - 1) + (y * 32)] > 1) {
                        rendered.pixels[x + (y * 32)] = 1
                    } else if (y > 0 && rendered.pixels[x + ((y - 1) * 32)] > 1) {
                        rendered.pixels[x + (y * 32)] = 1
                    } else if (x < 31 && rendered.pixels[x + 1 + (y * 32)] > 1) {
                        rendered.pixels[x + (y * 32)] = 1
                    } else if (y < 31 && rendered.pixels[x + ((y + 1) * 32)] > 1) {
                        rendered.pixels[x + (y * 32)] = 1
                    }
                }
            }
        }
        for (x in 31 downTo 0) {
            for (y in 31 downTo 0) {
                if (rendered.pixels[x + (y * 32)] == 0 && (x > 0) && (y > 0) && rendered.pixels[x - 1 + ((y - 1) * 32)] > 0) {
                    rendered.pixels[x + (y * 32)] = 0x302020
                }
            }
        }
        Graphics2D.drawRect(0, 0, 32, 32, 0xff00ff)
        Graphics2D.setTarget(raster, width, height)
        Graphics2D.setBounds(clipLeft, clipTop, clipRight, clipBottom)
        Graphics3D.centerX = centreX
        Graphics3D.centerY = centreY
        Graphics3D.offsets = scanOffsets

        return rendered
    }

    fun getObjSprite(id: Int): Sprite? {
        return getObj(id)?.let { getObjSprite(it) }
    }
}
