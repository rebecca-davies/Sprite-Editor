package sh.rebecca.inventory.editor

import javafx.beans.property.SimpleObjectProperty
import javafx.embed.swing.SwingNode
import javafx.scene.control.SelectionMode
import sh.rebecca.inventory.obj.ObjService
import sh.rebecca.inventory.model.ModelService
import tornadofx.*

class InventoryEditorStyle : Stylesheet() {
    init {
        Companion.root {
            prefWidth = 650.px
            prefHeight = 800.px
        }
        Companion.scrollPane {
            prefWidth = 500.px
        }
    }
}

class InventoryEditorView : View() {

    private val itemService: ObjService by di()
    private val scene: Scene by di()
    private val modelWrapper = SwingNode()
    private val selectedItem = SimpleObjectProperty<Int>()
    private val modelIds = (0 until itemService.getCount()).toList().toObservable()

    init {
        selectedItem.onChange { selected ->
            selected?.let { id ->
                runAsync {
                    itemService.getObj(id)?.let {
                        scene.obj = it
                    }
                }
            }
        }
    }

    override val root = borderpane {
        modelWrapper.content = this@InventoryEditorView.scene
        center = modelWrapper

        right = listview(modelIds) {
            selectionModel.selectionMode = SelectionMode.SINGLE
            bindSelected(this@InventoryEditorView.selectedItem)
        }
    }
}
