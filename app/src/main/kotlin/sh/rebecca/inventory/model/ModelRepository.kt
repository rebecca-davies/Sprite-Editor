package sh.rebecca.inventory.model

import com.displee.cache.CacheLibrary
import media.Model
import org.springframework.stereotype.Component
import sh.rebecca.inventory.repository.Repository
import sh.rebecca.inventory.definition.ObjReader
import java.io.ByteArrayInputStream

interface ModelRepository : Repository<Model>

@Component
class CacheModelRepository(private val reader: RSModelReader, private val cache: CacheLibrary) : ModelRepository {
    override fun findById(id: Int): Model? {
        TODO("Not yet implemented")
    }

    override fun getCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getModel(id: Int): Model? {
        return reader.read(ByteArrayInputStream(cache.data(1, id)!!))
    }
}
