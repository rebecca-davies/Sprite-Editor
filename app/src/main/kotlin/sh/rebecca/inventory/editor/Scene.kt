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
class Scene(private val objService: ObjService) : JComponent() {

    var obj = objService.getObj(1)!!

    init {
        this.maximumSize = Dimension(320, 320)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val viewport = ImageProducer3D(320, 320)
        viewport.bind()
        Graphics2D.fillRect(0, 0, 320, 320, 0xff00ff)
        setup()
        viewport.draw(graphics, 0, 0)
    }

    var mouseX = 0
    var mouseY = 0

    private fun setup() {
        objService.getObjSprite(obj)?.draw(0, 0, 320, 320)
        repaint()
    }
}
