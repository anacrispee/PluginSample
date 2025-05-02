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

![image](https://github.com/user-attachments/assets/b548935a-fe79-49c2-85af-e06f716e1d4e)


Centralize suas configurações de build no arquivo de Plugin:
```kotlin
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
```
#### Sobre o Plugin
O método `applyPlugins` é utilizado para aplicar plugins principais ao projeto:
* `kotlin-android`: aplica as configurações necessárias para projetos Android que usam Kotlin.

O método `setProjectConfig`, por sua vez, seta configurações do projeto:
* `defaultConfig`, configurações padrão:
  * `compileSdkVersion`: versão do SDK de compilação que será usado para compilar o código.
  * `minSdk`: versão mínima do Android necessária para rodar o app.
  * `targetSdk`: informa ao sistema Android que o app foi testado e é compatível até a API 35 (Android 14).
  * `versionCode`: usado internamente pelo Android para diferenciar versões. Esse número precisa aumentar a cada release para ser aceito na Play Store.
  * `versionName`: o nome da versão que aparece para o usuário.
  * `testInstrumentationRunner`: informa ao Gradle qual runner usar para rodar os testes Android.
* `compileOptions`, opções de compilação:
  * `sourceCompatibility` e `targetCompatibility`: versões do Java.
 
O bloco de `project.tasks.withType(KotlinCompile::class.java).configureEach` configura as opções do compilador Kotlin para que o `jvmTarget` seja o de valor desejado, garantindo que o código compilado seja compatível.

```kotlin
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
```
O trecho acima, por sua vez, tem como objetivo verificar se módulo é library (data, data_remote, data_local, etc, que foi criado) ou é o app (principal).
Ele configurará dois tipos de build: release e debug, com as seguintes propriedades:
* `isMinifyEnabled`: ativa o ProGuard ou R8 para ofuscar e reduzir o código.
* `isShrinkResources`: remove recursos (imagens, strings, etc.) não usados.
* `debuggable()`: impede depuração — essencial para builds de produção.
* `proguardFile(...)`: define qual arquivo .pro usar com regras personalizadas.

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
* [Custom Gradle Plugins in Android](https://williamkingsley.medium.com/custom-gradle-plugins-in-android-23342b98e721)
* [How to Build a Custom Gradle Plugin to Share Project Config - Multi-Module Architecture](https://www.youtube.com/watch?v=kFWmL5opJNk&ab_channel=PhilippLackner)
