package top.zztech.ainote

import org.babyfish.jimmer.client.EnableImplicitApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import top.zztech.ainote.cfg.EnvConfigInitializer

@SpringBootApplication
@EnableImplicitApi
class App

fun main(args: Array<String>) {
	// 在应用启动前加载 .env 文件
	EnvConfigInitializer.loadEnvFile()
	
	// 创建 SpringApplication 并注册初始化器
	val application = org.springframework.boot.SpringApplication(App::class.java)
	application.addInitializers(EnvConfigInitializer())
	application.run(*args)
}
