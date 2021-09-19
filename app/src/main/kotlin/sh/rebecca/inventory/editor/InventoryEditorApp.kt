package sh.rebecca.inventory

import javafx.application.Application
import javafx.stage.Stage
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import sh.rebecca.inventory.editor.InventoryEditorStyle
import sh.rebecca.inventory.editor.InventoryEditorView
import tornadofx.*
import kotlin.reflect.KClass

@SpringBootApplication
class InventoryEditorApp : App(InventoryEditorView::class, InventoryEditorStyle::class) {

    private lateinit var context: ConfigurableApplicationContext

    override fun start(stage: Stage) {
        stage.isResizable = false
        super.start(stage)
    }

    override fun init() {
        super.init()
        context = SpringApplicationBuilder(InventoryEditorApp::class.java).headless(false).web(WebApplicationType.NONE).run(*parameters.raw.toTypedArray())
        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = context.getBean(type.java)
            override fun <T : Any> getInstance(type: KClass<T>, name: String): T = context.getBean(type.java, name)
        }
    }

    override fun stop() {
        super.stop()
        context.close()
    }
}

fun main() {
    Application.launch(InventoryEditorApp::class.java)
}

