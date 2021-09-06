package sh.rebecca.inventory.model

import media.Model
import reader.ModelReader
import java.io.ByteArrayInputStream

fun ModelReader.read(data: ByteArray): Model {
    return this.read(ByteArrayInputStream(data))
}
