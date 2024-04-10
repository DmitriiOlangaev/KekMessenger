import androidx.navigation.safe.args.generator.ext.capitalize
import com.google.protobuf.gradle.GenerateProtoTask

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.devtools.ksp")
    id("com.google.protobuf") version "0.9.1"
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.demo.kekmessenger"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    sourceSets.all {
        java.srcDir("build/generated/source/proto/$name/java")
        kotlin.srcDir("build/generated/source/proto/$name/kotlin")
    }

    defaultConfig {
        applicationId = "com.demo.kekmessenger"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.retrofit)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.coil.base)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.protobuf.javalite)
    implementation(libs.adapterdelegates4.kotlin.dsl)
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.7"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }

            }
        }
    }
}


androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            // This is a workaround for https://issuetracker.google.com/301244513 which depends on internal
            // implementations of the android gradle plugin and the ksp gradle plugin which might change in the future
            // in an unpredictable way.
            val kspTaskName = "ksp${variant.name.capitalize()}Kotlin"
            val kspTask = project.tasks.getByName(kspTaskName) as org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool<*>
            val protoTask = project.tasks.getByName("generate${variant.name.capitalize()}Proto") as GenerateProtoTask
            kspTask.dependsOn(protoTask)
        }
    }
}

//tasks.named("kspDebugKotlin") {
//    dependsOn(tasks.named("generateDebugProto"))
//}
//tasks.named("kspReleaseKotlin") {
//    dependsOn(tasks.named("generateReleaseProto"))
//}
