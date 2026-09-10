import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Carga las credenciales de firma desde keystore.properties (en la raíz del proyecto).
// Así todos los APK (debug y release) se firman siempre con la misma clave y se pueden
// instalar encima sin "conflicto con un paquete".
val keystoreProperties = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.amcsoftware.sidebar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.amcsoftware.sidebar"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "040926"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("softpymes") {
            val storePath = keystoreProperties.getProperty("storeFile") ?: "keystore/softpymes.jks"
            storeFile = rootProject.file(storePath)
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("softpymes")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("softpymes")
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
    buildFeatures {
        viewBinding = true
    }

    //Nombre del APK , generado
    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = "SoftPymes-${variant.buildType.name}-${variant.versionName}.apk"
            }
    }

}

dependencies {
    implementation ("com.github.f0ris.sweetalert:library:1.6.2")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.itextpdf:itextg:5.5.10")
    implementation ("com.google.android.material:material:1.5.0")
    implementation ("androidx.activity:activity-ktx:1.9.0")
    implementation ("androidx.fragment:fragment-ktx:1.7.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("androidx.recyclerview:recyclerview:1.3.1");
    implementation ("androidx.navigation:navigation-fragment:2.3.0-alpha01")
    implementation ("androidx.navigation:navigation-ui:2.3.0-alpha01")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.annotation)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
}