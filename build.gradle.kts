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
 * The Android build runs inside `./gradlew ... | tee build.log` in CI, which swallows the
 * exit code, so a real compilation failure can leave the job looking green with no APK.
 * When running on GitHub Actions this finalizer recompiles with captured output and turns
 * every Kotlin/Java error into a `::error::` workflow command, which GitHub surfaces as an
 * annotation — so the failure and its cause are always visible in the run summary.
 *
 * It never runs on a normal local build (GITHUB_ACTIONS is not set).
 */
val manzilDiagnostics = tasks.register("manzilDiagnostics") {
    group = "verification"
    description = "Reprints Kotlin/Java compile errors as GitHub Actions annotations."

    doLast {
        val captured = java.io.ByteArrayOutputStream()
        val process = ProcessBuilder("./gradlew", ":app:compileDebugKotlin", "--console=plain")
            .directory(rootDir)
            .redirectErrorStream(true)
            .start()
        process.inputStream.copyTo(captured)
        val exit = process.waitFor()
        val text = captured.toString(Charsets.UTF_8.name())

        val errors = text.lines()
            .map { it.trim() }
            .filter { it.startsWith("e: ") || it.startsWith("error: ") || it.contains(" error: ") }
            .distinct()
            .take(40)

        if (errors.isEmpty() && exit != 0) {
            println("::error::compileDebugKotlin failed without a parsable error line")
        }
        errors.forEach { line ->
            println("::error::" + line.replace("\r", " ").take(400))
        }
        text.lines().takeLast(25).forEach { line ->
            if (line.isNotBlank()) println("::warning::gradle: " + line.take(300))
        }
    }
}

if (System.getenv("GITHUB_ACTIONS") == "true") {
    gradle.projectsEvaluated {
        listOf(":app").forEach { path ->
            findProject(path)?.tasks?.matching { task ->
                task.name == "assembleDebug" || task.name == "assembleRelease" ||
                    task.name == "testDebugUnitTest"
            }?.configureEach {
                finalizedBy(manzilDiagnostics)
            }
        }
    }
}
