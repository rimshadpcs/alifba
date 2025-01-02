 pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

}
 dependencyResolutionManagement {
     repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Change to PREFER_SETTINGS or PREFER_PROJECT
     repositories {
         google()
         mavenCentral()
     }
 }

rootProject.name = "Alifba"
include(":app")
