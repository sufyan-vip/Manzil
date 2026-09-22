plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

/**
 * CI diagnostics helper.
 *
 * The Android steps in CI run as `./gradlew ... | tee build.log`, which swallows the exit
 * code, so a build that failed to compile can leave the job looking green with no APK.
 * This finalizer reads the tee'd build/test logs and re-prints every compiler error as a
 * `::error::` workflow command, which GitHub shows as an annotation in the run summary.
 * Outside CI (no build.log in the project root) it does nothing.
 */
val manzilDiagnostics = tasks.register("manzilDiagnostics") {
    group = "verification"
    description = "Surfaces Kotlin/Java compile errors from the CI logs as GitHub annotations."

    doLast {
        val logs = listOf(rootProject.file("build.log"), rootProject.file("test.log"))
            .filter { it.exists() && it.length() > 0 }
        if (logs.isEmpty()) {
            println("Manzil diagnostics: no CI logs found, nothing to report.")
            return@doLast
        }

        val lines = logs.flatMap { it.readLines() }
        val errors = lines
            .map { it.trim() }
            .filter { it.startsWith("e: ") || it.startsWith("error:") || it.contains(" error: ") }
            .distinct()
            .take(40)

        val testFailures = lines
            .filter { it.contains(" FAILED") || it.contains("AssertionError") }
            .distinct()
            .take(30)

        if (errors.isEmpty() && testFailures.isEmpty()) {
            println("::notice::Manzil diagnostics: no compiler errors and no test failures in the CI logs.")
        }
        errors.forEach { line ->
            println("::error::" + line.replace("\r", " ").take(400))
        }
        testFailures.forEach { line ->
            println("::error::failing test: " + line.trim().take(300))
        }
        lines.takeLast(30).forEach { line ->
            if (line.isNotBlank()) println("::warning::gradle: " + line.trim().take(300))
        }
    }
}

gradle.projectsEvaluated {
    findProject(":app")?.tasks?.matching { task ->
        task.name == "assembleDebug" || task.name == "assembleRelease" ||
            task.name == "testDebugUnitTest" || task.name == "compileDebugKotlin"
    }?.configureEach {
        finalizedBy(manzilDiagnostics)
    }
}
