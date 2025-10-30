import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeHotReload)
  alias(libs.plugins.kotlinX.serialization.plugin)
  alias(libs.plugins.ktfmt)
  alias(libs.plugins.room)
  alias(libs.plugins.ksp)
  alias { libs.plugins.atomicfu }
}

ktfmt { googleStyle() }

// Room database configuration
room { schemaDirectory("$projectDir/schemas") }

kotlin {
  androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  jvm()

  js {
    browser()
    binaries.executable()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.executable()
  }

  // Hierarchy template configuration
  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  applyDefaultHierarchyTemplate {
    common {
      group("standalone") {
        withAndroidTarget()
        withJvm()
        group("ios") { withIos() }
      }
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(compose.preview)
      implementation(libs.androidx.activity.compose)
    }

    commonMain.dependencies {
      // Compose dependencies
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)

      // Lifecycle dependencies
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)

      // Kotlin extensions
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.json)

      // Navigation and UI
      implementation(libs.navigation.compose)
      implementation(libs.compose.icons.awesome)

      // Utilities
      implementation(libs.multiplatform.settings)

      // Backend and networking
      implementation(libs.ktor.client.core)
      implementation(libs.supabase.database)
      implementation(libs.supabase.auth)

      // Dependency injection
      implementation(libs.koin.core)
      implementation(libs.koin.compose)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.coroutines.test)
      implementation(libs.kotest.assertions)
    }

    @SuppressWarnings("unused")
    val standaloneMain by getting {
      dependencies {
        implementation(libs.androidx.room.runtime)
        implementation(libs.sqlite.bundled)
      }
      configurations { implementation { exclude(group = "org.jetbrains", module = "annotations") } }
    }

    androidMain.dependencies {
      implementation(compose.preview)
      implementation(libs.androidx.activity.compose)
      implementation(libs.ktor.client.okhttp)
    }

    androidUnitTest.dependencies { implementation(libs.androidx.test.core) }

    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutinesSwing)
      implementation(libs.ktor.client.java)
    }
  }

  sourceSets.all {
    languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
    languageSettings.optIn("kotlin.time.ExperimentalTime")
  }

  compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }
}

android {
  namespace = "proj.tarotmeter.axl"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "proj.tarotmeter.axl"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
  buildTypes { getByName("release") { isMinifyEnabled = false } }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {
  debugImplementation(compose.uiTooling)
  add("kspAndroid", libs.androidx.room.compiler)
  add("kspIosSimulatorArm64", libs.androidx.room.compiler)
  add("kspIosX64", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspJvm", libs.androidx.room.compiler)
}

compose.desktop {
  application {
    mainClass = "proj.tarotmeter.axl.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "proj.tarotmeter.axl"
      packageVersion = "1.0.0"
    }
  }
}

apply(from = "precompile.gradle.kts")
