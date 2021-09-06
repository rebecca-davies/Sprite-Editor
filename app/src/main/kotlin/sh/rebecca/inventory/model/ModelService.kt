package sh.rebecca.inventory.model

import media.Model
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class ModelService(private val repository: ModelRepository) {

    @Cacheable("models")
    fun getModel(id: Int): Model? {
        return repository.findById(id)
    }

    fun getModels(): List<Model> {
        return repository.findAll()
    }

    fun getModelCount(): Int {
        return repository.getCount()
    }
}
