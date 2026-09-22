plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// CI diagnostics: when a task fails, print its cause chain instead of leaving
// only a Gradle stack trace behind. The `gradlew` launcher turns these lines
// into GitHub Actions annotations, which stay readable through the check-runs
// API even when a step swallows the Gradle exit code.
gradle.taskGraph.afterTask { task ->
    val failure = task.state.failure
    if (failure != null) {
        var cause: Throwable? = failure
        var depth = 0
        while (cause != null && depth < 12) {
            val firstLine = cause.message?.lineSequence()?.firstOrNull()?.trim().orEmpty()
            logger.lifecycle("manzil-failure ${task.path}: ${cause.javaClass.name}: $firstLine")
            cause = cause.cause
            depth++
        }
    }
}
