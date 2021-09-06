package sh.rebecca.inventory

import com.displee.cache.CacheLibrary
import javafx.embed.swing.SwingNode
import sh.rebecca.inventory.component.ModelRenderer
import tornadofx.*

class InvEditor : View() {
    override val root = borderpane {
        val swingNode = SwingNode()
        swingNode.content = ModelRenderer()
        center = swingNode
    }
}

class InvEditorStyle : Stylesheet() {
    init {
        root {
            prefWidth = 650.px
            prefHeight = 800.px
        }
        scrollPane {
            prefWidth = 500.px
        }
    }
}

class InvEditorApp : App(InvEditor::class, InvEditorStyle::class)

fun main(args: Array<String>) {
    test()
    launch<InvEditorApp>(*args)
}

fun test() {

}

