import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
}

val jimmerVersion: String by rootProject.extra
val springBootVersion: String by rootProject.extra

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {

	implementation(project(":model"))
	implementation(project(":repository"))

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("org.springframework.boot:spring-boot-starter-web:${springBootVersion}")
	implementation("org.springframework.boot:spring-boot-starter-aop:${springBootVersion}")
	implementation("org.redisson:redisson-spring-boot-starter:3.52.0")
    // 运行时模块编译所需的 Spring Security 核心与 Web（供过滤器/工具类使用）
    implementation("org.springframework.security:spring-security-core:6.4.2")
    implementation("org.springframework.security:spring-security-web:6.4.2")

    // 引入 JJWT（API + 运行时实现与 Jackson 支持）
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("org.springframework.kafka:spring-kafka:3.3.4")
	implementation("org.apache.kafka:connect-api:0.10.0.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict", "-Xmulti-dollar-interpolation")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
