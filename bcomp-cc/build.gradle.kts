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