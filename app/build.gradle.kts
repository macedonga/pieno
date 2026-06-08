plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.ceccon.pieno"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.ceccon.pieno"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        vectorDrawables.useSupportLibrary = true

        // Redirect OAuth registrato lato IdP: it.insiel.benzapp.cittadino:/oauth2redirect
        // AppAuth registra il RedirectUriReceiverActivity tramite questo placeholder.
        manifestPlaceholders["appAuthRedirectScheme"] = "it.insiel.benzapp.cittadino"

        // Google Wallet: URL del Worker Cloudflare che firma il pass (vedi
        // wallet-worker/). La chiave del service account vive solo nel Worker, MAI
        // nell'APK. URL pubblico (non e' un segreto), usato di default da ogni build;
        // si puo' sovrascrivere con -PwalletSignerUrl=...
        buildConfigField(
            "String",
            "WALLET_SIGNER_URL",
            "\"${project.findProperty("walletSignerUrl") ?: "https://pieno-wallet-signer.ceccon.workers.dev"}\"",
        )
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

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.animation)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.appauth)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.zxing.core)
    implementation(libs.cbor)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode)
    implementation(libs.play.services.pay)
    implementation(libs.osmdroid)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation("junit:junit:4.13.2")
}
