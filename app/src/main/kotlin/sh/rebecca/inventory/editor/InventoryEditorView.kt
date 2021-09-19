package sh.rebecca.inventory.editor

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.embed.swing.SwingNode
import javafx.scene.control.SelectionMode
import sh.rebecca.inventory.obj.Obj
import sh.rebecca.inventory.obj.ObjService
import tornadofx.*

class InventoryEditorStyle : Stylesheet() {
    init {
        Companion.root {
            prefWidth = 650.px
            prefHeight = 800.px
        }
        Companion.scrollPane {
            prefWidth = 400.px
        }
    }
}

class InventoryEditorView : View() {

    private val itemService: ObjService by di()
    private val scene: Scene by di()
    private val modelWrapper = SwingNode()
    private val selectedItem = SimpleObjectProperty<Obj>()
    private val items = FXCollections.observableArrayList((0 until itemService.getCount()).mapNotNull {
        if(itemService.getObj(it)!!.name.isNotEmpty()) itemService.getObj(it) else null
    }).toObservable()

    init {
        title = "Inventory Tool"
        selectedItem.onChange { selected ->
            scene.obj = selected!!
        }
    }

    override val root = borderpane {
        modelWrapper.content = this@InventoryEditorView.scene
        center = modelWrapper
        right = listview(items) {
            prefWidth = 200.0
            selectionModel.selectionMode = SelectionMode.SINGLE
            bindSelected(this@InventoryEditorView.selectedItem)
            cellFormat {
                text = "${it?.id}: ${it?.name}"
            }
        }

        top = menubar {
            menu("File") {
                item("Open")
                item("Save")
            }
            menu("Edit") {
                item("Undo")
                item("Redo")
            }
            menu("Help") {
                item("Github")
                item("About")
            }
        }

    }


}
