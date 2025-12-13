/*
 * Copyright (c) 2025 zhang zhen
 * Created on 2025-12-13
 *
 * 环境变量配置 - 加载 .env 文件
 * Environment Configuration - Load .env file
 */

package top.zztech.ainote.cfg

import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import java.io.File

/**
 * 环境变量配置初始化器
 * 在 Spring Boot 启动早期加载 .env 文件中的环境变量
 */
class EnvConfigInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(EnvConfigInitializer::class.java)
        private var initialized = false
        
        /**
         * 加载 .env 文件到系统环境变量
         * 此方法可以在 Spring Boot 启动前调用
         */
        fun loadEnvFile() {
            if (initialized) {
                return
            }
            
            try {
                // 查找项目根目录下的 .env 文件
                val projectRoot = File(".").canonicalFile
                val envFile = File(projectRoot, ".env")
                val parentEnvFile = File(projectRoot.parentFile, ".env")
                
                val dotenv = when {
                    envFile.exists() -> {
                        logger.info("加载 .env 文件: ${envFile.absolutePath}")
                        dotenv {
                            directory = envFile.parent ?: "."
                            filename = ".env"
                            ignoreIfMissing = false
                            systemProperties = true  // 自动加载到系统属性中，Spring Boot 可以读取
                        }
                    }
                    parentEnvFile.exists() -> {
                        logger.info("加载 .env 文件: ${parentEnvFile.absolutePath}")
                        dotenv {
                            directory = parentEnvFile.parent ?: ".."
                            filename = ".env"
                            ignoreIfMissing = false
                            systemProperties = true
                        }
                    }
                    else -> {
                        logger.warn("未找到 .env 文件，将使用系统环境变量或默认值")
                        dotenv {
                            ignoreIfMissing = true
                            systemProperties = true
                        }
                    }
                }
                logger.info("环境变量配置加载完成")
                initialized = true
            } catch (e: Exception) {
                logger.error("加载 .env 文件时出错: ${e.message}", e)
                // 不抛出异常，允许应用继续启动
            }
        }
    }
    
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        loadEnvFile()
    }
}

/**
 * 环境变量配置类
 */
@Configuration
class EnvConfig {
    // 环境变量加载逻辑在 EnvConfigInitializer 中实现
    // 使用 ApplicationContextInitializer 确保在 Spring 配置加载前加载环境变量
}
