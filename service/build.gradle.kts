import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
	id("com.google.devtools.ksp")
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

val jimmerVersion: String by rootProject.extra

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {

	implementation(project(":repository"))
	implementation(project(":runtime"))

	ksp("org.babyfish.jimmer:jimmer-ksp:${jimmerVersion}")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
	runtimeOnly("org.postgresql:postgresql:42.7.4")
	runtimeOnly("io.lettuce:lettuce-core:6.4.2.RELEASE")
	runtimeOnly("com.github.ben-manes.caffeine:caffeine:2.9.1")
	
	implementation("com.aliyun.oss:aliyun-sdk-oss:3.18.1")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// Without this configuration, gradle command can still run.
// However, Intellij cannot find the generated source.
kotlin {
	sourceSets.main {
		kotlin.srcDir("build/generated/ksp/main/kotlin")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}