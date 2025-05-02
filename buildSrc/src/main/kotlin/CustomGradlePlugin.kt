import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class CustomGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        applyPlugins(project)
        setProjectConfig(project)
    }

    private fun applyPlugins(project: Project) {
        project.apply {
            plugin("kotlin-android")
        }
    }

    private fun setProjectConfig(project: Project) {
        project.android().apply {

            compileSdkVersion(35)

            defaultConfig {
                minSdk = 24
                targetSdk = 35
                versionCode = 1
                versionName = "1.0"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_18
                targetCompatibility = JavaVersion.VERSION_18
            }

            project.tasks.withType(KotlinCompile::class.java).configureEach {
                kotlinOptions {
                    jvmTarget = "18"
                }
            }

            val proguardFile = "proguard-rules.pro"
            when (this) {
                is LibraryExtension -> defaultConfig {
                    consumerProguardFiles(proguardFile)
                }

                is AppExtension -> buildTypes {
                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        debuggable(false)
                        proguardFile(proguardFile)
                    }

                    getByName("debug") {
                        isMinifyEnabled = false
                        isShrinkResources = false
                        debuggable(true)
                        proguardFile(proguardFile)
                    }
                }
            }
        }
    }

    private fun Project.android() : BaseExtension {
        return project.extensions.getByName("android") as BaseExtension
    }
}