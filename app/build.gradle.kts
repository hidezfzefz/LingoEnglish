plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

fun configValue(key: String): String {
    val fromEnv = providers.environmentVariable(key).orNull?.trim().orEmpty()
    if (fromEnv.isNotEmpty()) return fromEnv
    val local = providers.fileContents(
        rootProject.layout.projectDirectory.file("local.properties"),
    ).asText.orNull.orEmpty()
    local.lineSequence().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.startsWith("$key=")) return trimmed.substringAfter("=").trim()
    }
    return ""
}

fun quoted(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.lingo.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.lingo.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 9
        versionName = "1.8"

        buildConfigField("String", "SUPABASE_URL", quoted(configValue("LINGO_SUPABASE_URL")))
        buildConfigField("String", "SUPABASE_ANON_KEY", quoted(configValue("LINGO_SUPABASE_ANON_KEY")))
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            // AGP 9's package-scoped R8 optimization produced an inaccessible
            // Kotlin stdlib superclass at runtime. Keep R8 off for this build;
            // the native LiveKit libraries dominate the APK size anyway.
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")

    implementation(platform("io.github.jan-tennert.supabase:bom:3.8.0"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.ktor:ktor-client-okhttp:3.5.1")

    implementation("io.livekit:livekit-android:2.29.1")
}

// Refuse to produce a shipping APK that would show the "calls are offline"
// card. Debug builds may stay unconfigured for layout work.
gradle.taskGraph.whenReady {
    val releaseRequested = allTasks.any {
        it.name == "assembleRelease" || it.name == "bundleRelease" || it.name == "installRelease"
    }
    if (releaseRequested) {
        val url = configValue("LINGO_SUPABASE_URL")
        val key = configValue("LINGO_SUPABASE_ANON_KEY")
        if (url.isBlank() || key.isBlank()) {
            throw GradleException(
                "Release builds need LINGO_SUPABASE_URL and LINGO_SUPABASE_ANON_KEY. " +
                    "Run scripts/configure_live.sh first.",
            )
        }
    }
}
