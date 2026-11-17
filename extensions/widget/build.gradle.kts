plugins {
    alias(libs.plugins.kotlin.compose)
}

extension {
    name = "extensions/widget.mpe"
}

android {
    namespace = "app.ssch71.extension.widget"
    compileSdk = 37
    defaultConfig {
        minSdk = 33
    }
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
    buildTypes {
        release {
            // 라이브러리를 전부 우리 패키지로 옮겨서 셔클의 Compose/코루틴과 충돌 방지
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.remote.core)
    implementation(libs.remote.creation)
    implementation(libs.remote.creation.core)
    implementation(libs.remote.creation.compose)
    implementation(libs.remote.material3)
    implementation(libs.glance.wear)
    implementation(libs.glance.wear.core)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.remote.creation.compose.ExperimentalRemoteCreationComposeApi")
    }
}
