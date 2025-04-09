import java.util.Properties

// At the top of build.gradle.kts
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}
val githubToken: String = localProperties.getProperty("github.token") ?: ""
val logmealApiKey: String = localProperties.getProperty("logmeal.api.key") ?: ""
val spoonacularApiKey: String = localProperties.getProperty("spoonacular.api.key") ?: ""



plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}


android {
    namespace = "com.example.vassilis88.fridgechef"
    compileSdk = 34
    buildFeatures {
        buildConfig = true  // Enable BuildConfig feature
    }

    defaultConfig {
        applicationId = "com.example.vassilis88.fridgechef"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "GITHUB_TOKEN", "\"$githubToken\"")
        buildConfigField("String", "LOGMEAL_API_KEY", "\"$logmealApiKey\"")
        buildConfigField("String", "SPOONACULAR_API_KEY", "\"$spoonacularApiKey\"")



        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}



dependencies {


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.picasso)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.volley)



    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}