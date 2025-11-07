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

        // ⚠️ Repositório oficial do Spotify (App Remote + Auth)
        maven {
            url = uri("https://maven.pkg.github.com/spotify/android-sdk")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(System.getenv("USERNAME"))
                    .get()
                password = providers.gradleProperty("gpr.key")
                    .orElse(System.getenv("TOKEN"))
                    .get()

            }
        }
    }
}

rootProject.name = "My Application"
include(":app")
