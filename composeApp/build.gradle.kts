import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.composeHotReload)
}

// Configure JDK 21 toolchain for the project
tasks.withType<JavaCompile>().configureEach {
    javaCompiler.set(
        project.javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }.get()
    )
}

composeCompiler {
    includeSourceInformation = true
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop") {
        // JVM toolchain is configured at the extension level
    }
    
    // Use JVM 21 to match Gradle's JDK
    jvmToolchain(21)
    
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.room.ktx)
                implementation(libs.androidx.work.runtime.ktx)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.google.fit.services)
                implementation(libs.google.fit.auth)
                implementation(libs.kotlinx.coroutines.play.services)
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"
        
        // Enable Hot Reload
        jvmArgs += listOf("-Dcompose.hot.reload.enabled=true")
        jvmArgs += listOf("-Dcompose.hot.reload.logStdout=true")
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "WeightIT"
            packageVersion = "1.0.0"
            
            macOS {
                bundleID = "org.example.project"
            }
            
            windows {
                menuGroup = "WeightIT"
                upgradeUuid = "18159995-d967-4cd2-8885-77BFA97CFA9F"
            }
        }
    }
}

android {
    namespace = "org.example.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
}

dependencies {
    debugImplementation(compose.uiTooling)
}

