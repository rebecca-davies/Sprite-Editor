package sh.rebecca.inventory.model

import com.displee.cache.CacheLibrary
import media.Model
import org.springframework.stereotype.Component
import sh.rebecca.inventory.repository.Repository

interface ModelRepository : Repository<Model>

private const val modelIndex = 1

@Component
class CacheModelRepository(private val reader: RSModelReader, private val cache: CacheLibrary) : ModelRepository {

    override fun findById(id: Int): Model? {
        val data = cache.data(modelIndex, id)
        return data?.let { reader.read(it) }
    }

    override fun findAll(): List<Model> {
        return cache.index(modelIndex).archives().mapNotNull { archive ->
            archive.file(0)?.data?.let { reader.read(it) }
        }
    }

    override fun getCount(): Int {
        return cache.index(modelIndex).archives().size
    }
}
