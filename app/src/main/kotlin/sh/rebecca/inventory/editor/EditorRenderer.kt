package sh.rebecca.inventory.editor

import javafx.embed.swing.SwingNode
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.layout.Priority
import tornadofx.*
import tornadofx.Stylesheet.Companion.left
import tornadofx.Stylesheet.Companion.root
import java.awt.Panel
import javax.swing.JPanel

class EditorRenderer : View() {

    private val scene: Scene by di()
    private val modelWrapper = SwingNode()
    override val root = borderpane {
        center {
            modelWrapper.content = this@EditorRenderer.scene
            add(modelWrapper)
        }
        bottom {
            vbox {
                button("Test")
                alignment = Pos.TOP_CENTER
            }
        }
    }
}
