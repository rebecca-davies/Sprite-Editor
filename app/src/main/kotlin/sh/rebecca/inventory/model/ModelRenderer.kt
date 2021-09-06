package sh.rebecca.inventory.model

import com.displee.cache.CacheLibrary
import image.Graphics3D
import image.ImageProducer3D
import media.Model
import org.springframework.stereotype.Component
import reader.ModelReader
import java.awt.Graphics
import java.io.ByteArrayInputStream
import javax.swing.JComponent

@Component
class ModelRenderer(private val reader: ModelReader, private val library: CacheLibrary) : JComponent() {

    private val model: Model = reader.read(ByteArrayInputStream(library.data(7, 20000)!!))

    var cameraPitch = 128
    var cameraYaw = 0
    var rotation = 0;

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
