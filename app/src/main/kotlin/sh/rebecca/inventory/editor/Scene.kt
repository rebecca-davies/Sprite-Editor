package sh.rebecca.inventory.editor

import com.displee.cache.CacheLibrary
import image.Graphics2D
import image.ImageProducer3D
import javafx.embed.swing.JFXPanel
import org.springframework.stereotype.Component
import reader.ModelReader
import sh.rebecca.inventory.input.fixRotation
import sh.rebecca.inventory.obj.ObjService
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.*
import java.awt.event.MouseEvent.*
import javax.swing.JComponent

@Component
class Scene(private val objService: ObjService) : JFXPanel() {

    var obj = objService.getObj(1042)!! //test

    init {
        this.addMouseMotionListener(this.drag())
        this.addMouseWheelListener(this.scroll())
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val viewport = ImageProducer3D(width, height)
        viewport.bind()
        Graphics2D.fillRect(0, 0, width, height, 0xff00ff)
        setup()
        viewport.draw(graphics, 0, 0)
    }

    var mouseX = 0
    var mouseY = 0

    private fun setup() {
        objService.getObjSprite(obj)?.draw(0, 0, width, height)
        repaint()
    }

    fun drag() = object: MouseMotionListener {
        override fun mouseDragged(e: MouseEvent?) {
            when(e!!.button) {
                BUTTON1 -> {
                    if (e.y < mouseY) obj.roll -= (mouseY - e.y) else if (e.y > mouseY) obj.roll += (e.y - mouseY)
                    if (e.x < mouseX) obj.yaw += (mouseX - e.x) else if (e.x > mouseX) obj.yaw -= (e.x - mouseX)
                    mouseX = e.x
                    mouseY = e.y
                    obj.yaw = fixRotation(obj.yaw)
                    obj.roll = fixRotation(obj.roll)
                    repaint()
                }

                BUTTON3 -> {
                    if (e.y < mouseY) obj.translateY -= 1 else if (e.y > mouseY) obj.translateY += 1
                    if (e.x < mouseX) obj.translateX -= 1 else if (e.x > mouseX) obj.translateX += 1
                    mouseX = e.x
                    mouseY = e.y
                }
            }
        }

        override fun mouseMoved(e: MouseEvent?) {
        }
    }
    fun scroll() = MouseWheelListener { e -> obj.zoom += e!!.wheelRotation * 25 }
}
