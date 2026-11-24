import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { alias(libs.plugins.android.application); alias(libs.plugins.kotlin.compose) }
// AGP 9 has built-in Kotlin support: the org.jetbrains.kotlin.android plugin must NOT be applied.
// Release signing is driven by environment (TOKILEARN_KEYSTORE / TOKILEARN_STORE_PASSWORD /
// TOKILEARN_KEY_ALIAS / TOKILEARN_KEY_PASSWORD), same pattern as TokiMusic/TokiDiscover.
// Without the env vars the release build falls back to the debug key so it always compiles.
// No keystore is ever committed to this repo.
val releaseKeystore: String? = System.getenv("TOKILEARN_KEYSTORE")

// TokiUI is the design source of truth. The parser + token JSON are snapshotted
// under android/tokiui/ (same idea as TokiMusic) because this public repo
// cannot see the private TokiUI checkout in CI. Fonts live in src/main/res/font.
val tokiSnapshotDir = rootProject.file("tokiui")
val tokiTokens = tokiSnapshotDir.resolve("tokens/tokiui.semantic.tokens.json")
check(tokiTokens.isFile) {
    "TokiUI snapshot is missing at ${tokiSnapshotDir.absolutePath} (see android/tokiui/SYNC.md)"
}

val generatedTokiRes = layout.buildDirectory.dir("generated/tokiuiRes")
generatedTokiRes.get().asFile.mkdirs()

val syncTokiUiResources = tasks.register("syncTokiUiResources") {
    group = "tokiui"
    description = "Write the launcher color from semantic color.seed."
    inputs.file(tokiTokens)
    outputs.dir(generatedTokiRes)
    doLast {
        val seed = Regex(""""seed"\s*:\s*"(#[0-9A-Fa-f]{6})"""")
            .find(tokiTokens.readText())
            ?.groupValues
            ?.get(1)
            ?: error("TokiUI color.seed is missing from $tokiTokens")
        val values = generatedTokiRes.get().asFile.resolve("values")
        values.mkdirs()
        values.resolve("toki_seed_colors.xml").writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <!-- Generated from TokiUI tokens/tokiui.semantic.tokens.json color.seed. Do not edit. -->
                <color name="toki_seed">#FF${seed.removePrefix("#").uppercase()}</color>
            </resources>
            """.trimIndent() + "\n",
        )
    }
}

android {
    namespace = "com.tokilearn.app"
    compileSdk = 37 // Compose 1.12 / core-ktx 1.19 dependencies require compiling against API 37+
    defaultConfig {
        applicationId = "com.tokilearn.app"
        // TokiUI Android target is Android 16+ (minSdk 36), same as TokiStudio/TokiMusic.
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = file(releaseKeystore)
                storePassword = System.getenv("TOKILEARN_STORE_PASSWORD")
                keyAlias = System.getenv("TOKILEARN_KEY_ALIAS") ?: "tokilearn"
                keyPassword = System.getenv("TOKILEARN_KEY_PASSWORD")
            }
        }
    }
    buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"); signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug") } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { compose = true; buildConfig = true }
    sourceSets.named("main") {
        // Official TokiUI parser + token JSON, snapshotted under android/tokiui
        // because this public repo cannot see the private TokiUI checkout in CI.
        kotlin.directories.add("../tokiui/src/main/kotlin")
        assets.directories.add("../tokiui/tokens")
        res.directories.add(generatedTokiRes.get().asFile.absolutePath)
    }
}
kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
tasks.named("preBuild").configure { dependsOn(syncTokiUiResources) }
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.ui); implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation); implementation(libs.androidx.compose.animation); implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.haze); implementation(libs.haze.blur)
    implementation(libs.coil.compose); implementation(libs.okhttp)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.org.json)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.org.json)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
