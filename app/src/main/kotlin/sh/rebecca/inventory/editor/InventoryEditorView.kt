package sh.rebecca.inventory.editor

import javafx.beans.property.SimpleObjectProperty
import javafx.embed.swing.SwingNode
import javafx.scene.control.SelectionMode
import sh.rebecca.inventory.model.ItemService
import sh.rebecca.inventory.model.ModelRenderer
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

class InventoryEditorView() : View() {

    private val itemService: ItemService by di()
    private val modelService: ModelService by di()
    private val modelRenderer: ModelRenderer by di()
    private val modelWrapper = SwingNode()
    private val selectedItem = SimpleObjectProperty<Int>()
    private val modelIds = (0 until itemService.getCount()).toList().toObservable()

    init {
        selectedItem.onChange { selected ->
            selected?.let { id ->
                runAsync {
                    itemService.getObj(id)?.let {
                        modelRenderer.obj = it
                    }
                }
            }
        }
    }

    override val root = borderpane {
        modelWrapper.content = modelRenderer
        center = modelWrapper

        right = listview(modelIds) {
            selectionModel.selectionMode = SelectionMode.SINGLE
            bindSelected(this@InventoryEditorView.selectedItem)
        }
    }
}
