package sh.rebecca.inventory.model

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import sh.rebecca.inventory.definition.Obj

@Service
class ItemService(private val repository: ItemRepository) {

    @Cacheable("items")
    fun getObj(id: Int): Obj? {
        return repository.findById(id)
    }

    fun getCount(): Int {
        return repository.getCount()
    }
}
