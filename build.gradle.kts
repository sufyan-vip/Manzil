plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
}

buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

gradle.buildFinished {
    val failure = this.failure
    if (failure != null) {
        var cause: Throwable? = failure
        val causes = mutableListOf<String>()
        while (cause != null) {
            causes.add("${cause.javaClass.simpleName}: ${cause.message}")
            cause = cause.cause
        }
        val fullMsg = causes.joinToString(" -> ")
        println("::error title=GradleBuildFailure::${fullMsg.take(500)}")
    } else {
        println("::notice title=GradleBuildSuccess::Build finished with SUCCESS for tasks: ${gradle.startParameter.taskNames}")
    }
}
