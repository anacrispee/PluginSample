# 🧩 PluginSample
## 🇧🇷 Português

Um projeto exemplo de Plugin Gradle feito para centralizar e compartilhar lógica de configuração para todos os módulos em um projeto Android multimódulo.

Este plugin é útil pois centraliza toda a configuração de build em um só lugar, facilitando a manutenção e escalabilidade de um aplicativo, e, consequentemente, diminuindo a inconsistência e retrabalho ao gerenciar esses arquivos e configurações.

## Criação do Plugin
### 1. Criando o módulo
Na visão de Projeto do Android Studio, crie um módulo de nome "_buildSrc_" clicando com botão direito sobre o package principal do projeto > New > Module ou Directory.

Caso escolha criar um Directory, terá de manualmente criar um novo arquivo `build.gradle.kts` para o buildSrc criado, aplicando o seguinte trecho:
```kotlin
plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}
```
* `java-gradle-plugin:` Gradle espera que você esteja criando um plugin personalizado.
* `kotlin-dsl:` permite que você escreva o build script em Kotlin, em vez de usar Groovy.

### 2. Criando a classe Plugin
Agora você pode criar a sua classe de Plugin no caminho `src/main/kotlin`, que irá conter sua lógica de configuração.

Na visão de projeto ficará parecido com isto:

![image](https://github.com/user-attachments/assets/fd559d96-b7db-43e7-a492-812d04915826)

Centralize suas configurações de build no arquivo de Plugin:
```kotlin
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
            plugin("android-library")
            plugin("kotlin-android")
        }
    }

    private fun setProjectConfig(project: Project) {
        project.android().apply {
            defaultConfig {
                minSdk = ProjectConfig.MIN_SDK
                compileSdk = ProjectConfig.COMPILE_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
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

            project.tasks.withType(KotlinCompile::class.java).configureEach {
                kotlinOptions {
                    jvmTarget = "18"
                }
            }
        }
    }

    private fun Project.android() : LibraryExtension {
        return extensions.getByType(LibraryExtension::class.java)
    }
}
```
#### Sobre o Plugin
O método `applyPlugins` é utilizado para aplicar plugins principais ao projeto:
* `android-library`: usado em projetos que criam bibliotecas Android, aplicando as configurações básicas para isto.
* `kotlin-android`: aplica as configurações necessárias para projetos Android que usam Kotlin.

O método `setProjectConfig`, por sua vez, seta configurações do projeto:
* `defaultConfig`, configurações padrão:
  * `minSdk`: versão mínima do Android necessária para rodar o app
  * `compileSdk`: SDK de compilação que será usado para compilar o código
  * `testInstrumentationRunner`: informa ao Gradle qual runner usar para rodar os testes Android.
* `compileOptions`, opções de compilação:
  * `sourceCompatibility` e `targetCompatibility`: versão do Java.
* `buildTypes`, comportamento para os tipos de build como _release_ ou _debug_.

Já o bloco de `project.tasks.withType(KotlinCompile::class.java).configureEach` configura as opções do compilador Kotlin para que o `jvmTarget` seja o de valor desejado, garantindo que o código compilado seja compatível.

### 3. Declarando o Plugin
No arquivo `build.gradle.kts` do módulo de `buildSrc`, declare e configure o Plugin que está sendo criado com o seguinte trecho:
```kotlin
gradlePlugin {
    plugins {
        register("custom-gradle-plugin") {
            id = "custom-gradle-plugin"
            implementationClass = "CustomGradlePlugin"
        }
    }
}
```
#### Sobre o registro do Plugin
* `gradlePlugin`: este bloco é onde será registrado o plugin com o Gradle.
* `register("custom-gradle-plugin")`: registra o plugin com o nome indicado dentro das aspas.
* `id`: ID do plugin, é com ele que você aplica o plugin em projetos Gradle.
* `implementationClass`: define a classe de implementação do plugin, onde está a lógica dele.

O arquivo `build.gradle.kts` do `buildSrc` completo ficará assim:
```kotlin
plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("custom-gradle-plugin") {
            id = "custom-gradle-plugin"
            implementationClass = "CustomGradlePlugin"
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
    implementation("com.android.tools.build:gradle:8.6.0")
}
```
### 4. Aplicando o plugin em outros módulos
Remova a lógica de configuração do módulo em que você quer aplicar o plugin, deixando apenas o _namespace_, as dependências usadas por aquele módulo, as libs nativas necessárias e chame o plugin pelo seu id, ficando assim:
```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("custom-gradle-plugin") // 📌 Aplica o plugin criado
}

android {
    namespace = "com.example.data"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```
## Referências
* [Using Plugins - Gradle Documentation](https://docs.gradle.org/current/userguide/plugins.html)
* [Understanding Plugins - Gradle Documentation](https://docs.gradle.org/current/userguide/custom_plugins.html)
* [How to Build a Custom Gradle Plugin to Share Project Config - Multi-Module Architecture](https://www.youtube.com/watch?v=kFWmL5opJNk&ab_channel=PhilippLackner)

--- 

## 🇺🇸 English
An example Gradle plugin project designed to centralize and share configuration logic across all modules in a multi-module Android project.

This plugin is useful as it centralizes all build configurations in one place, making it easier to maintain and scale an app. It also reduces inconsistencies and repetitive work when managing these configuration files.

## Plugin Creation

### 1. Creating the `buildSrc` module

In Android Studio’s Project view, create a new module or directory named `buildSrc`.

If you choose to create a directory, manually create a `build.gradle.kts` file inside it and add the following:

```kotlin
plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}
```

- `java-gradle-plugin`: tells Gradle you're creating a custom plugin.
- `kotlin-dsl`: enables Kotlin syntax in the Gradle build script.

### 2. Creating the Plugin class

Now create a Kotlin class inside `src/main/kotlin` with your plugin logic.

Example:

```kotlin
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
            plugin("android-library")
            plugin("kotlin-android")
        }
    }

    private fun setProjectConfig(project: Project) {
        project.android().apply {
            defaultConfig {
                minSdk = ProjectConfig.MIN_SDK
                compileSdk = ProjectConfig.COMPILE_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
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

            project.tasks.withType(KotlinCompile::class.java).configureEach {
                kotlinOptions {
                    jvmTarget = "18"
                }
            }
        }
    }

    private fun Project.android() : LibraryExtension {
        return extensions.getByType(LibraryExtension::class.java)
    }
}
```

#### About the Plugin class

- `applyPlugins`: applies essential plugins like `android-library` and `kotlin-android`.
- `setProjectConfig`: sets project-wide configurations like:
  - `defaultConfig`: minimum SDK, compile SDK, and test runner.
  - `compileOptions`: Java version compatibility.
  - `buildTypes`: release build behavior.
  - `KotlinCompile`: ensures Kotlin uses JVM target 18.

### 3. Declaring the Plugin

Inside `build.gradle.kts` of `buildSrc`, declare your plugin like this:

```kotlin
gradlePlugin {
    plugins {
        register("custom-gradle-plugin") {
            id = "custom-gradle-plugin"
            implementationClass = "CustomGradlePlugin"
        }
    }
}
```

#### Explanation
- `register("custom-gradle-plugin")`: registers the plugin.
- `id`: the ID used to apply the plugin in other modules.
- `implementationClass`: the fully qualified name of the plugin class.

Full file example:

```kotlin
plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("custom-gradle-plugin") {
            id = "custom-gradle-plugin"
            implementationClass = "CustomGradlePlugin"
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
    implementation("com.android.tools.build:gradle:8.6.0")
}
```

### 4. Applying the Plugin in Other Modules

Once your plugin is ready, remove duplicate configuration logic from your module-level `build.gradle.kts` files and apply your custom plugin:

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("custom-gradle-plugin") // 📌 Applies your custom plugin
}

android {
    namespace = "com.example.data"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```
## References
* [Using Plugins - Gradle Documentation](https://docs.gradle.org/current/userguide/plugins.html)
* [Understanding Plugins - Gradle Documentation](https://docs.gradle.org/current/userguide/custom_plugins.html)
* [How to Build a Custom Gradle Plugin to Share Project Config - Multi-Module Architecture](https://www.youtube.com/watch?v=kFWmL5opJNk&ab_channel=PhilippLackner)
