package sh.rebecca.inventory.model

import com.displee.cache.CacheLibrary
import media.Model
import org.springframework.stereotype.Component
import sh.rebecca.inventory.definition.Obj
import sh.rebecca.inventory.definition.ObjReader
import sh.rebecca.inventory.repository.Repository

interface ItemRepository : Repository<Obj>


@Component
class CacheItemRepository(private val reader: RSModelReader, private val cache: CacheLibrary, private val obj: ObjReader) : ItemRepository {

    override fun findById(id: Int): Obj? {
        return obj.lookup(id)
    }

    override fun getCount(): Int {
        return obj.count
    }

    override fun getModel(id: Int): Obj? {
        TODO("Not yet implemented")
    }


}

