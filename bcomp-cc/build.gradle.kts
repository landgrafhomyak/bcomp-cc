plugins {
    kotlin("multiplatform") version "1.7.10"
}

repositories {
    mavenCentral()
}

group = "io.github.landgrafhomyak.itmo"
version = "1.0"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    val hostOs = System.getProperty("os.name")
    when {
        hostOs == "Mac OS X"         -> macosX64("native")
        hostOs == "Linux"            -> linuxX64("native")
        hostOs.startsWith("Windows") -> mingwX64("native")
        else                         -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    js(BOTH) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = false
            }
        }
        nodejs {}
    }



    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        all {
            languageSettings.optIn("kotlin.contracts.ExperimentalContracts")
        }

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}