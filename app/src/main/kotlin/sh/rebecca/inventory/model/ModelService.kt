package sh.rebecca.inventory.model

import media.Model
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import sh.rebecca.inventory.definition.Obj


@Service
class ModelService(private val repository: ModelRepository) {

    @Cacheable("models")
    fun getModel(id: Int): Model? {
        return repository.getModel(id)
    }
}
