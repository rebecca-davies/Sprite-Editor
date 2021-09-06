package sh.rebecca.inventory

import javafx.application.Application
import javafx.embed.swing.SwingNode
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import sh.rebecca.inventory.model.ModelRenderer
import tornadofx.*
import kotlin.reflect.KClass

class InvEditor : View() {

    private val modelWrapper = SwingNode()
    private val modelRenderer: ModelRenderer by di()

    override val root = borderpane {
        modelWrapper.content = modelRenderer
        center = modelWrapper
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

@SpringBootApplication
class InvEditorApp : App(InvEditor::class, InvEditorStyle::class) {

    lateinit var context: ConfigurableApplicationContext

    override fun init() {
        super.init()
        context = SpringApplicationBuilder(InvEditorApp::class.java).headless(false).web(WebApplicationType.NONE).run(*parameters.raw.toTypedArray())
        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = context.getBean(type.java)
            override fun <T : Any> getInstance(type: KClass<T>, name: String): T = context.getBean(type.java, name)
        }
    }

    override fun stop() { // On stop, we have to stop spring as well
        super.stop()
        context.close()
    }
}

fun main() {
    Application.launch(InvEditorApp::class.java)
}

