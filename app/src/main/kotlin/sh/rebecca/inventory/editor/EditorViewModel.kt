package sh.rebecca.inventory.editor

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.springframework.stereotype.Component
import sh.rebecca.inventory.obj.Obj
import tornadofx.toObservable

@Component
class EditorViewModel {
    var selectedItem = SimpleObjectProperty<Obj>()
    var items = FXCollections.observableArrayList<Obj>()
}
