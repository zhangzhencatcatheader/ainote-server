/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-11-29
 *
 * OSS配置 - 阿里云对象存储配置
 * OSS Configuration - Aliyun Object Storage Service configuration
 */

package top.zztech.ainote.cfg

import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 阿里云OSS配置属性
 */

@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
class OssProperties {
    var endpoint: String = ""
    var accessKeyId: String = ""
    var accessKeySecret: String = ""
    var bucketName: String = ""
}

/**
 * OSS客户端配置
 */
@Configuration
class OssConfig {

    @Bean
    @ConditionalOnProperty(
        prefix = "aliyun.oss",
        name = ["access-key-id", "access-key-secret"],
        matchIfMissing = false
    )
    fun ossClient(ossProperties: OssProperties): OSS {
        if (ossProperties.accessKeyId.isBlank() || ossProperties.accessKeySecret.isBlank()) {
            throw IllegalStateException("OSS配置不完整，请检查 access-key-id 和 access-key-secret")
        }
        return OSSClientBuilder().build(
            ossProperties.endpoint,
            ossProperties.accessKeyId,
            ossProperties.accessKeySecret
        )
    }
}
