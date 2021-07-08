dependencies {
    compile(project(":OrionAPI"))

    compile("com.google.inject:guice:${rootProject.extra["guice_version"]}") {
        exclude(group = "com.google.guava", module = "guava") // Supplied by Paper server
    }
}

val compileJava by tasks.getting(JavaCompile::class) {
    options.compilerArgs.add("-proc:none")
}