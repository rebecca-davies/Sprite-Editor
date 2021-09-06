package sh.rebecca.inventory.component

import com.displee.cache.CacheLibrary
import image.Graphics3D
import image.ImageProducer3D
import media.Model
import reader.ModelReader
import sh.rebecca.inventory.model.decode
import util.Colors
import java.awt.Graphics
import java.io.File
import javax.swing.JComponent

class ModelRenderer : JComponent() {

    var cameraPitch = 128
    var cameraYaw = 0
    var rotation = 0;

    val library = CacheLibrary("C:\\Users\\Mikan\\jagexcache\\oldschool\\LIVE")
    val testModel = library.data(7, 18932)
    val model = decode(testModel!!)

    override fun paintComponent(g: Graphics) {
        var viewport = ImageProducer3D(650, 800)
        viewport.bind()
        Graphics3D.createPalette(1.0)
        Graphics3D.texturedShading = false
        setup()
        viewport.draw(graphics, 0, 0)
    }

    private fun setup() {

        model.calculateBoundaries()
        model.calculateNormals()
        model.calculateLighting(64, 768, 100, 100, 100)
        Model.frameTriangleCount = 0
        model.draw(0, rotation, 0, 0, 100, 400, 100)
        rotation += 4
        if (rotation >= 2048) {
            rotation = 0
        }
        repaint()
    }
}
