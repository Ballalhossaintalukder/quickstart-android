import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.android.application") version "8.11.1" apply false
    id("com.android.library") version "8.11.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.firebase.crashlytics") version "3.0.4" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    id("androidx.navigation.safeargs") version "2.9.2" apply false
    id("com.github.ben-manes.versions") version "0.52.0" apply true
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0" apply false
}

allprojects {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.49.1") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

tasks.register<JavaExec>("ktlintCheck") {
    val outputDir = "${project.buildDir}/reports/ktlint/"
    val inputFiles = project.fileTree("src").include("**/*.kt")
    val outputFile = "${outputDir}ktlint-checkstyle-report.xml"

    // See: https://medium.com/@vanniktech/making-your-gradle-tasks-incremental-7f26e4ef09c3
    inputs.files(inputFiles)
    outputs.file(outputFile)

    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")

    args(
        "--format",
        "--code-style=android_studio",
        "--reporter=plain",
        "--reporter=checkstyle,output=${outputFile}",
        "**/*.kt"
    )

    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}

fun notFromFirebase(candidate: ModuleComponentIdentifier): Boolean {
    return candidate.group != "com.google.firebase"
}

fun isNonStable(candidate: ModuleComponentIdentifier): Boolean {
    return listOf("alpha", "beta", "rc", "snapshot", "-m", "final").any { keyword ->
        keyword in candidate.version.lowercase()
    }
}

fun isBlockListed(candidate: ModuleComponentIdentifier): Boolean {
    return listOf(
            "androidx.browser:browser",
            "com.facebook.android",
            "com.google.guava",
            "com.github.bumptech.glide",
            "com.google.android.gms"
    ).any { keyword ->
        keyword in candidate.toString().lowercase()
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        (isNonStable(candidate) && notFromFirebase(candidate)) || isBlockListed(candidate)
    }
}

tasks {
    register("clean", Delete::class) {
        delete(rootProject.buildDir)
    }
}
