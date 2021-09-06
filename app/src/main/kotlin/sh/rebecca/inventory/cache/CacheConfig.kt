package sh.rebecca.inventory.cache

import com.displee.cache.CacheLibrary
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "inventorytool")
class CacheConfig(var cacheDir: String = "") {

    @Bean
    fun getCacheLibrary(): CacheLibrary {
        return CacheLibrary(cacheDir)
    }
}
