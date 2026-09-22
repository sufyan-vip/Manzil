plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
}

gradle.taskGraph.afterTask { task, state ->
    if (state.failure != null) {
        val rootCause = generateSequence(state.failure as? Throwable) { it.cause }.lastOrNull()
        val msg = (rootCause?.message ?: state.failure?.message ?: "unknown error").replace("\n", " ").take(400)
        println("::error title=FailedTask ${task.path}::$msg")
    }
}

buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
