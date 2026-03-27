pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // web3j → tech.pegasys:jc-kzg-4844 (not on Maven Central)
        maven {
            url = uri("https://artifacts.consensys.net/public/maven/maven/")
        }
    }
}

rootProject.name = "TranzoCustody"
include(":app")
