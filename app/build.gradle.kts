import java.util.Properties

// NOTE: For production-ready Firebase integration, the real 'google-services.json' file
// must be downloaded from the Firebase Console for the project 'lyo-food-delivery'
// and placed inside the 'app' module at 'app/google-services.json'.
// This is the single primary Firebase Android configuration source for com.lyo.fooddelivery.

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

val secretsProperties = Properties().apply {
  val envFile = file("${rootDir}/.env")
  if (envFile.exists()) {
    envFile.inputStream().use { load(it) }
  } else {
    val exampleFile = file("${rootDir}/.env.example")
    if (exampleFile.exists()) {
      exampleFile.inputStream().use { load(it) }
    }
  }
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.lyo.fooddelivery"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    val googleServicesFile = file("${projectDir}/google-services.json")
    var parsedProjectId = ""
    var parsedStorageBucket = ""
    var parsedAppId = ""
    var parsedApiKey = ""

    if (googleServicesFile.exists()) {
      val text = googleServicesFile.readText()
      fun extractValue(regexStr: String): String {
        return Regex(regexStr).find(text)?.groupValues?.get(1) ?: ""
      }
      parsedProjectId = extractValue("\"project_id\":\\s*\"([^\"]+)\"")
      parsedStorageBucket = extractValue("\"storage_bucket\":\\s*\"([^\"]+)\"")
      parsedAppId = extractValue("\"mobilesdk_app_id\":\\s*\"([^\"]+)\"")
      parsedApiKey = extractValue("\"current_key\":\\s*\"([^\"]+)\"")
    }

    val firebaseApiKey = parsedApiKey.ifEmpty { "AIzaSyCj35HL7MOIQMc1sXV1AGcRGt7DSkRabXk" }
    val firebaseAppId = parsedAppId.ifEmpty { "1:604469873807:android:ae70018a97af4cfab2b4fa" }
    val firebaseProjectId = parsedProjectId.ifEmpty { "lyo-ai-food-delivery" }
    val firebaseStorageBucket = parsedStorageBucket.ifEmpty { "lyo-ai-food-delivery.firebasestorage.app" }
    val firebaseDatabaseUrl = "https://$firebaseProjectId-default-rtdb.firebaseio.com"

    buildConfigField("String", "FIREBASE_API_KEY", "\"$firebaseApiKey\"")
    buildConfigField("String", "FIREBASE_APP_ID", "\"$firebaseAppId\"")
    buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$firebaseProjectId\"")
    buildConfigField("String", "FIREBASE_DATABASE_URL", "\"$firebaseDatabaseUrl\"")
    buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"$firebaseStorageBucket\"")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  bundle {
    language { enableSplit = true }
    density { enableSplit = true }
    abi { enableSplit = true }
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      isDebuggable = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.messaging)
  implementation(libs.firebase.appcheck.playintegrity)
  implementation(libs.firebase.appcheck.debug)
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.play.services.auth)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
