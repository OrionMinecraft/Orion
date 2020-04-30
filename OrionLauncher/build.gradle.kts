import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    compile(project(":OrionCore"))
}

val compileJava by tasks.getting(JavaCompile::class) {
    options.compilerArgs.add("-proc:none")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes(
                "Main-Class" to "eu.mikroskeem.orion.launcher.Bootstrap"
        )
    }
}

val shadowJar by tasks.getting(ShadowJar::class) {
    // Exclude unneeded files
    exclude("licenses/**")
    exclude("publicsuffixes.gz")
    exclude("META-INF/maven/**")
}

tasks["build"].dependsOn(shadowJar)
