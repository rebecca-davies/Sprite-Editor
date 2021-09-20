package sh.rebecca.inventory.editor

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.embed.swing.SwingNode
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*

class EditorRenderer : View() {

    private val scene: Scene by di()
    private val modelWrapper = SwingNode()
    private val editorViewModel: EditorViewModel by di()
    val objZoomLabel = SimpleStringProperty("Model zoom")
    val objZoom = SimpleIntegerProperty()
    val translateXLabel = SimpleStringProperty("Translate X")
    val objTranslateX = SimpleIntegerProperty()
    val translateYLabel = SimpleStringProperty("Translate Y")
    val objTranslateY = SimpleIntegerProperty()
    val pitchLabel = SimpleStringProperty("Pitch")
    val pitch = SimpleIntegerProperty()
    val yawLabel = SimpleStringProperty("Yaw")
    val yaw = SimpleIntegerProperty()
    val rollLabel = SimpleStringProperty("Roll")
    val roll = SimpleIntegerProperty()

    override val root = stackpane {
            modelWrapper.content = this@EditorRenderer.scene
            vbox {
                paddingAll = 25
                paddingTop = 120
                vbox {
                    label(objZoomLabel)
                    slider(0..2048) {
                        bind(objZoom)
                    }
                    alignment = Pos.BASELINE_CENTER
                    paddingTop = 5
                }
                vbox {
                    label(translateXLabel)
                    slider(-250..250) {
                        bind(objTranslateX)
                    }
                    alignment = Pos.CENTER
                    paddingTop = 5
                }
                vbox {
                    label(translateYLabel)
                    slider(-250..250) {
                        bind(objTranslateY)
                    }
                    alignment = Pos.CENTER
                    paddingTop = 5
                }
                vbox {
                    label(pitchLabel)
                    slider(0..2047) {
                        bind(pitch)
                    }
                    alignment = Pos.CENTER
                    paddingTop = 5
                }
                vbox {
                    label(yawLabel)
                    slider(0..2047) {
                        bind(yaw)
                    }
                    alignment = Pos.CENTER
                    paddingTop = 5
                }
                vbox {
                    label(rollLabel)
                    slider(0..2047) {
                        bind(roll)
                    }
                    alignment = Pos.CENTER
                    paddingTop = 5
                }
                vgrow = Priority.ALWAYS
                alignment = Pos.BOTTOM_CENTER
            }
            add(modelWrapper)
            alignment = Pos.TOP_CENTER
    }

    init {
        editorViewModel.selectedItem.onChange {
            if (it != null) {
                objZoom.value = it.zoom
                objTranslateX.value = it.translateX
                objTranslateY.value = it.translateY
                pitch.value = it.pitch
                yaw.value = it.yaw
                roll.value = it.roll
            }
        }
        objZoom.onChange {
            objZoomLabel.value = "Model Zoom: $it"
            editorViewModel.selectedItem.get()?.zoom = it
        }
        objTranslateX.onChange {
            translateXLabel.value = "Translate X: $it"
            editorViewModel.selectedItem.get()?.translateX = it
        }
        objTranslateY.onChange {
            translateYLabel.value = "Translate Y: $it"
            editorViewModel.selectedItem.get()?.translateY = it
        }
        pitch.onChange {
            pitchLabel.value = "Pitch: $it"
            editorViewModel.selectedItem.get()?.pitch = it
        }
        yaw.onChange {
            yawLabel.value = "Yaw: $it"
            editorViewModel.selectedItem.get()?.yaw = it
        }
        roll.onChange {
            rollLabel.value = "Roll: $it"
            editorViewModel.selectedItem.get()?.roll = it
        }

    }
}
