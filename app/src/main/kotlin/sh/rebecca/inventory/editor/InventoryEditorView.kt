package sh.rebecca.inventory.editor

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.layout.Priority
import sh.rebecca.inventory.obj.Obj
import sh.rebecca.inventory.obj.ObjService
import tornadofx.*

class InventoryEditorStyle : Stylesheet() {
    init {
        root {
            prefWidth = 520.px
            prefHeight = 800.px
        }
        scrollPane {
            prefWidth = 200.px
        }
    }
}

class InventoryEditorView : View() {

    private val itemService: ObjService by di()
    private val scene: Scene by di()
    private val selectedItem = SimpleObjectProperty<Obj>()
    private var items = FXCollections.observableArrayList((0 until itemService.getCount()).mapNotNull {
        if (itemService.getObj(it)!!.name.isNotEmpty()) itemService.getObj(it) else null
    }).toObservable()
    private var list = ListView(items)


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
                       list.items = items.filter { obj ->
                           obj.name.contains(new) || obj.id == new.toIntOrNull()
                       }.toObservable()
                   }
               }
               list = listview(items) {
                   prefWidth = 200.0
                   selectionModel.selectionMode = SelectionMode.SINGLE
                   bindSelected(this@InventoryEditorView.selectedItem)
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
        selectedItem.onChange {
            if (it != null) scene.obj = it
        }
    }
}


