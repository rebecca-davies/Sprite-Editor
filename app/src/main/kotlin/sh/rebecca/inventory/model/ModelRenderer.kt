package sh.rebecca.inventory.model

import com.displee.cache.CacheLibrary
import image.Graphics2D
import image.Graphics3D
import image.ImageProducer3D
import media.Model
import org.springframework.stereotype.Component
import reader.ModelReader
import sh.rebecca.inventory.definition.Obj
import sh.rebecca.inventory.definition.ObjReader
import sh.rebecca.inventory.input.fixRotation
import java.awt.Graphics
import java.awt.event.*
import java.awt.event.MouseEvent.*
import java.io.ByteArrayInputStream
import javax.swing.JComponent

@Component
class ModelRenderer(private val reader: ModelReader, private val objReader: ObjReader, private val library: CacheLibrary) : JComponent() {


    private final val obj = objReader.lookup(1050)
    var model: Model = reader.read(ByteArrayInputStream(library.data(1, obj.model)!!))

    init {
        this.addMouseMotionListener(this.drag())
        this.addMouseWheelListener(this.scroll())
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val viewport = ImageProducer3D(width, height)
        viewport.bind()
        Graphics2D.fillRect(0, 0, width, height, 0xffffff)
        Graphics3D.createPalette(1.0)
        Graphics3D.texturedShading = false
        setup()
        viewport.draw(graphics, 0, 0)
    }

    var rotationX = 0
    var rotationY = 0
    var mouseX = 0
    var mouseY = 0
    var zoom = 200
    var sceneX = 0
    var sceneY = 0

    private fun setup() {
        model.calculateBoundaries()
        model.calculateNormals()
        model.calculateLighting(64, 768, 100, 100, 100)
        model.draw(obj.pitch, obj.yaw, 0, obj.translateX, obj.translateY, obj.zoom, 100)
        repaint()
    }

    fun drag() = object: MouseMotionListener {
        override fun mouseDragged(e: MouseEvent?) {
            when(e!!.button) {
                BUTTON1 -> {
                    if (e.y < mouseY) rotationY -= (mouseY - e.y) else if (e.y > mouseY) rotationY += (e.y - mouseY)
                    if (e.x < mouseX) rotationX += (mouseX - e.x) else if (e.x > mouseX) rotationX -= (e.x - mouseX)
                    mouseX = e.x
                    mouseY = e.y
                    rotationX = fixRotation(rotationX)
                    rotationY = fixRotation(rotationY)
                }

                BUTTON3 -> {
                    if (e.y < mouseY) sceneY -= 5 else if (e.y > mouseY) sceneY += 5
                    if (e.x < mouseX) sceneX -= 5 else if (e.x > mouseX) sceneX += 5
                    mouseX = e.x
                    mouseY = e.y
                }
            }
        }

        override fun mouseMoved(e: MouseEvent?) {
        }
    }
    fun scroll() = object: MouseWheelListener {
        override fun mouseWheelMoved(e: MouseWheelEvent?) {
            zoom += e!!.wheelRotation * 25
        }

    }


}


