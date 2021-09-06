package sh.rebecca.inventory.cache

import com.displee.cache.CacheLibrary
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "inventorytool")
class CacheConfig(var cacheDir: String = "") {

    @Bean
    fun cacheLibrary(): CacheLibrary {
        return CacheLibrary(cacheDir)
    }

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager()
        cacheManager.setCaffeine(Caffeine.newBuilder().maximumSize(100))
        return cacheManager
    }
}
