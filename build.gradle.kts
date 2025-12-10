plugins {
    kotlin("jvm") version "2.1.20" apply false
    kotlin("plugin.spring") version "2.1.20" apply false
    kotlin("plugin.allopen") version "2.1.20" apply false
    id("com.google.devtools.ksp") version "2.1.20-2.0.0" apply false
    id("org.springframework.boot") version "3.5.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

val jimmerVersion by extra { "0.9.117" }
val springBootVersion by extra { "3.5.6" }

allprojects {
	group = "top.zztech.ainote"
	version = "1.0.0"
}