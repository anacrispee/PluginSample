plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("custom-gradle-plugin")
}

android {
    namespace = "com.example.data_remote"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}