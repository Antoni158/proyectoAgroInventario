plugins {

    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.ksp)

    id("com.google.gms.google-services")
}

// resolver conflictos bouncycastle

configurations.all {

    resolutionStrategy {

        force(
            "org.bouncycastle:bcprov-jdk18on:1.78.1"
        )

        force(
            "org.bouncycastle:bcpkix-jdk18on:1.78.1"
        )

        force(
            "org.bouncycastle:bcutil-jdk18on:1.78.1"
        )

        dependencySubstitution {

            substitute(
                module(
                    "org.bouncycastle:bcprov-jdk15to18"
                )
            ).using(
                module(
                    "org.bouncycastle:bcprov-jdk18on:1.78.1"
                )
            )

            substitute(
                module(
                    "org.bouncycastle:bcprov-jdk15on"
                )
            ).using(
                module(
                    "org.bouncycastle:bcprov-jdk18on:1.78.1"
                )
            )

            substitute(
                module(
                    "org.bouncycastle:bcutil-jdk15to18"
                )
            ).using(
                module(
                    "org.bouncycastle:bcutil-jdk18on:1.78.1"
                )
            )

            substitute(
                module(
                    "org.bouncycastle:bcpkix-jdk15to18"
                )
            ).using(
                module(
                    "org.bouncycastle:bcpkix-jdk18on:1.78.1"
                )
            )
        }
    }

    exclude(
        group = "org.bouncycastle",
        module = "bcprov-jdk15to18"
    )

    exclude(
        group = "org.bouncycastle",
        module = "bcprov-jdk15on"
    )

    exclude(
        group = "org.bouncycastle",
        module = "bcprov-ext-jdk15to18"
    )
}

android {

    namespace =
        "com.example.inventario"

    compileSdk = 35

    defaultConfig {

        applicationId =
            "com.example.inventario"

        minSdk = 26

        targetSdk = 35

        versionCode = 1

        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        release {

            isMinifyEnabled = false

            proguardFiles(

                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),

                "proguard-rules.pro"
            )
        }
    }

    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17

        isCoreLibraryDesugaringEnabled =
            true
    }

    kotlinOptions {

        jvmTarget = "17"
    }

    buildFeatures {

        compose = true
    }

    packaging {

        resources {

            excludes +=
                "META-INF/DEPENDENCIES"

            excludes +=
                "META-INF/LICENSE*"

            excludes +=
                "META-INF/NOTICE*"

            excludes +=
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF"

            excludes +=
                "META-INF/versions/11/OSGI-INF/MANIFEST.MF"

            pickFirsts +=
                "org/bouncycastle/LICENSE.class"

            pickFirsts +=
                "org/bouncycastle/LICENSE"
        }
    }
}

dependencies {

    // material

    implementation(
        libs.androidx.material3
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        "androidx.compose.material3:material3-window-size-class"
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    // core

    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.7.0"
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"
    )

    implementation(
        libs.androidx.activity.compose
    )

    // compose

    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.compose.foundation
    )

    implementation(
        libs.androidx.compose.foundation.layout
    )

    // navigation

    implementation(
        libs.androidx.navigation.compose
    )

    // room

    implementation(
        libs.androidx.room.runtime
    )

    ksp(
        libs.androidx.room.compiler
    )

    implementation(
        libs.androidx.room.ktx
    )

    // firebase

    implementation(
        platform(
            "com.google.firebase:firebase-bom:33.7.0"
        )
    )

    implementation(
        "com.google.firebase:firebase-database"
    )

    implementation(
        "com.google.firebase:firebase-messaging"
    )

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0"
    )

    // excel

    implementation(
        "org.apache.poi:poi:5.2.5"
    ) {

        exclude(
            group = "org.bouncycastle"
        )
    }

    implementation(
        "org.apache.poi:poi-ooxml:5.2.5"
    ) {

        exclude(
            group = "org.bouncycastle"
        )
    }

    // pdf

    implementation(
        "com.tom-roush:pdfbox-android:2.0.27.0"
    ) {

        exclude(
            group = "org.bouncycastle"
        )
    }

    // bouncycastle

    implementation(
        "org.bouncycastle:bcprov-jdk18on:1.78.1"
    )

    implementation(
        "org.bouncycastle:bcpkix-jdk18on:1.78.1"
    )

    // desugar

    coreLibraryDesugaring(
        "com.android.tools:desugar_jdk_libs:2.1.3"
    )

    // tests

    testImplementation(
        libs.junit
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
}