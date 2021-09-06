package sh.rebecca.inventory.model

import io.guthix.buffer.readSmallSmart
import io.netty.buffer.Unpooled
import media.Model
import org.springframework.stereotype.Component
import reader.ModelReader
import sh.rebecca.inventory.model.rs317.decode317Model
import java.io.InputStream

@Component
class RSModelReader : ModelReader() {
        override fun read(stream: InputStream): Model {
        val modelData = stream.readBytes()
        stream.close()
        return decode317Model(modelData)
    }
}
