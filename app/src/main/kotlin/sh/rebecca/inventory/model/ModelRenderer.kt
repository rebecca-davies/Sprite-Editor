package sh.rebecca.inventory.model

import com.displee.cache.CacheLibrary
import image.Graphics2D
import image.Graphics3D
import image.ImageProducer3D
import media.Model
import org.springframework.stereotype.Component
import reader.ModelReader
import sh.rebecca.inventory.input.fixRotation
import tornadofx.observable
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.io.ByteArrayInputStream
import javax.swing.JComponent
import kotlin.math.max
import kotlin.math.min

@Component
class ModelRenderer(private val reader: ModelReader, private val library: CacheLibrary) : JComponent() {

    var model: Model = reader.read(ByteArrayInputStream(library.data(7, 20000)!!))
    var cameraPitch = 128
    var cameraYaw = 0
    var rotation = 0;

    init {
        this.addMouseMotionListener(this.drag())
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val viewport = ImageProducer3D(width, height)
        viewport.bind()
        Graphics3D.createPalette(1.0)
        Graphics3D.texturedShading = false
        setup()
        viewport.draw(graphics, 0, 0)
    }

    var rX = 0
    var rY = 0

    private fun setup() {
        model.calculateBoundaries()
        model.calculateNormals()
        model.calculateLighting(64, 768, 100, 100, 100)
        Model.frameTriangleCount = 0
        model.draw(rY, rX, 0, 0, 100, 400, 100)
        repaint()
    }

    var mX = 0
    var mY = 0


    fun drag() = object: MouseMotionListener {
        override fun mouseDragged(e: MouseEvent?) {
            if(e!!.y < mY) rY -= (mY - e.y) else if(e.y > mY) rY += (e.y - mY)
            if(e.x < mX) rX += (mX - e.x) else if(e.x > mX) rX -= (e.x - mX)
            mX = e.x
            mY = e.y
            rX = fixRotation(rX)
            rY = fixRotation(rY)

        }

        override fun mouseMoved(e: MouseEvent?) {

        }
    }


}


