package top.zztech.ainote.runtime.cache

import com.fasterxml.jackson.databind.ObjectMapper
import org.babyfish.jimmer.meta.ImmutableProp
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.sql.cache.Cache
import org.babyfish.jimmer.sql.cache.CacheFactory
import org.babyfish.jimmer.sql.cache.redis.spring.RedisCacheCreator
import org.babyfish.jimmer.sql.cache.redisson.RedissonCacheLocker
import org.babyfish.jimmer.sql.cache.redisson.RedissonCacheTracker
import org.babyfish.jimmer.sql.kt.cache.AbstractKCacheFactory
import org.redisson.api.RedissonClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@ConditionalOnProperty("spring.redis.host")
@Configuration
class CacheConfig {

    @Bean
    fun cacheFactory(
        redissonClient: RedissonClient,
        connectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper
    ): CacheFactory {

        val creator = RedisCacheCreator(connectionFactory, objectMapper)
            .withRemoteDuration(Duration.ofHours(1))
            .withLocalCache(100, Duration.ofMinutes(5))
            .withMultiViewProperties(40, Duration.ofMinutes(2), Duration.ofMinutes(24))
            .withSoftLock(
                RedissonCacheLocker(redissonClient),
                Duration.ofSeconds(30)
            )
            .withTracking(
                RedissonCacheTracker(redissonClient)
            )

        return object : AbstractKCacheFactory() {

            override fun createObjectCache(type: ImmutableType): Cache<*, *>? =
                creator.createForObject<Any, Any>(type)

            override fun createAssociatedIdCache(prop: ImmutableProp): Cache<*, *>? =
                creator.createForProp<Any, Any>(prop, filterState.isAffected(prop.targetType))

            override fun createAssociatedIdListCache(prop: ImmutableProp): Cache<*, List<*>>? =
                creator.createForProp<Any, List<*>>(prop, filterState.isAffected(prop.targetType))

            override fun createResolverCache(prop: ImmutableProp): Cache<*, *>? =
                creator.createForProp<Any, Any>(prop, true)
        }
    }
}
