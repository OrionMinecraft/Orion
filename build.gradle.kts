plugins {
    `java-library`
    id("net.minecrell.licenser") version "0.4.1"
    id("com.github.johnrengelman.shadow") version "5.2.0" apply false
}

// Dependency versions
rootProject.extra["orion_at_version"] = "0.0.1-SNAPSHOT"
rootProject.extra["picomaven_version"] = "0.0.3-SNAPSHOT"
rootProject.extra["guice_version"] = "4.2.0"
rootProject.extra["gson_version"] = "2.8.0"
rootProject.extra["guava_version"] = "21.0"
rootProject.extra["shuriken_version"] = "0.0.1-SNAPSHOT"
rootProject.extra["hocon_version"] = "3.6.1"
rootProject.extra["mixin_version"] = "0.8.1-SNAPSHOT"
rootProject.extra["modLauncherVersion"] = "5.1.0"
rootProject.extra["log4j2_version"] = "2.8.1"
rootProject.extra["asm_version"] = "8.0.1"
rootProject.extra["checker_qual_version"] = "3.3.0"

// Subproject configurations
allprojects {
    // Project information
    group = "eu.mikroskeem"
    version = "0.0.4-SNAPSHOT"
    description = "Orion"

    // Repositories
    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.wut.ee/repository/mikroskeem-repo")
        maven("https://papermc.io/repo/repository/maven-public")
        maven("https://repo.spongepowered.org/maven")
    }
}

subprojects {
    apply(plugin = "net.minecrell.licenser")
    apply(plugin = "java-library")

    // Licenser task
    license {
        header = rootProject.file("etc/HEADER")
        filter.include("**/*.java")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

defaultTasks("build")