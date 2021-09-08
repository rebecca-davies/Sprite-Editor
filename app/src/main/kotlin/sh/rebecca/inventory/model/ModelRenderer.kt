package sh.rebecca.inventory.model

import com.displee.cache.CacheLibrary
import image.Graphics2D
import image.Graphics3D
import image.ImageProducer3D
import org.springframework.stereotype.Component
import reader.ModelReader
import sh.rebecca.inventory.definition.ObjReader
import sh.rebecca.inventory.input.fixRotation
import java.awt.Graphics
import java.awt.event.*
import java.awt.event.MouseEvent.*
import java.io.ByteArrayInputStream
import javax.swing.JComponent

@Component
class ModelRenderer(private val reader: ModelReader, private val objReader: ObjReader, private val library: CacheLibrary) : JComponent() {


    var obj = objReader.lookup(0) //test


    init {
        this.addMouseMotionListener(this.drag())
        this.addMouseWheelListener(this.scroll())
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val viewport = ImageProducer3D(width, height)
        viewport.bind()
        Graphics2D.fillRect(0, 0, width, height, 0xffffff)
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
       /* val model = reader.read(ByteArrayInputStream(library.data(1, obj.model)!!))
        Graphics3D.createPalette(1.0);
        Graphics3D.texturedShading = false;
        model.calculateBoundaries()
        model.calculateNormals()
        model.calculateLighting(64, 768, -50, -10, -50)
        val sin: Int = (Graphics3D.sin[obj.pitch] * obj.zoom) shr 16
        val cos: Int = (Graphics3D.cos[obj.pitch] * obj.zoom) shr 16
        model?.draw(0, obj.yaw, obj.roll, obj.pitch, obj.translateX, sin + (model.minBoundY / 2) + obj.translateY, cos + obj.translateY)*/

        objReader.sprite(obj.id, 0, 0)?.draw(100, 100)
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


