plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// llama.cpp 클론 디렉토리. 우선순위:
//   1) gradle.properties 또는 -PllamaCppDir=...
//   2) 환경변수 LLAMA_CPP_DIR
//   3) ~/llama.cpp (기본값)
val llamaCppDir: String = (
    findProperty("llamaCppDir") as String?
        ?: System.getenv("LLAMA_CPP_DIR")
        ?: "${System.getProperty("user.home")}/llama.cpp"
)

android {
    namespace = "gpt.in.your.hand"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "gpt.in.your.hand"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // arm64-v8a 단일 ABI (책 12장 빌드 산출물과 일치)
            abiFilters += listOf("arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++17", "-fexceptions", "-frtti")
                arguments += listOf(
                    "-DLLAMA_CPP_DIR=$llamaCppDir",
                )
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    sourceSets {
        getByName("main") {
            // native/prebuilt/{ABI}/*.so 를 APK에 포함
            jniLibs.srcDirs("${rootProject.projectDir}/native/prebuilt")
        }
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}