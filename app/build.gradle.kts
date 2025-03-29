plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.anand.hope"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.anand.hope"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures.viewBinding = true
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.1")) // Use Firebase BOM
    implementation("com.google.firebase:firebase-database-ktx") // No version needed

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.preference.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation("com.google.android.gms:play-services-measurement-api:21.0.0") // Downgrade from 22.4.0
    implementation("com.google.firebase:firebase-analytics")

    implementation(libs.play.services.location.v2101)

    implementation("com.google.maps.android:android-maps-utils:2.3.0")

    implementation("com.google.android.gms:play-services-nearby:18.3.0")

    // Material Components
    implementation("com.google.android.material:material:1.11.0")

    // ViewBinding
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("androidx.core:core-ktx:1.9.0")

    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.libraries.places:places:4.1.0")

    implementation ("org.osmdroid:osmdroid-android:6.1.14")





}
