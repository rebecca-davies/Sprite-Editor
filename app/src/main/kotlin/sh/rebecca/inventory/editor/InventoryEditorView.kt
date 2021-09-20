package sh.rebecca.inventory.editor

import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.layout.Priority
import sh.rebecca.inventory.obj.ObjService
import tornadofx.*
import java.util.*

class InventoryEditorStyle : Stylesheet() {
    init {
        root {
            prefWidth = 520.px
            prefHeight = 600.px
        }
        scrollPane {
            prefWidth = 200.px
        }
    }
}

class InventoryEditorView : View() {

    private val itemService: ObjService by di()
    private val editorViewModel: EditorViewModel by di()
    private val scene: Scene by di()

    private var list = ListView(editorViewModel.items)


   override val root = borderpane {

       top {
           menubar {
               menu("File") {
                   item("Open Cache",)
                   item("Open Model")
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
       center {
           add(EditorRenderer())
       }

       right {
           vbox {
               textfield("Search...") {
                   textProperty().addListener { obs, old, new ->
                       list.items = editorViewModel.items.filter { obj ->
                           obj.name.lowercase().contains(new.lowercase()) || obj.id == new.toIntOrNull()
                       }.toObservable()
                   }
               }
               list = listview(editorViewModel.items) {
                   prefWidth = 200.0
                   selectionModel.selectionMode = SelectionMode.SINGLE
                   bindSelected(editorViewModel.selectedItem)
                   cellFormat {
                       text = "${it?.id}: ${it?.name}"
                   }

                   vgrow = Priority.ALWAYS
               }
               alignment = Pos.BOTTOM_RIGHT
           }
       }
   }
    init {
        title = "Inventory Tool"
        editorViewModel.selectedItem.value = scene.obj
        editorViewModel.selectedItem.onChange {
            if (it != null) {
                scene.obj = it
            }
        }
        editorViewModel.items.addAll((0 until itemService.getCount()).mapNotNull {
            if (itemService.getObj(it)!!.name.isNotEmpty()) {
                itemService.getObj(it)
            } else {
                null
            }
        }.toList())
    }
}


