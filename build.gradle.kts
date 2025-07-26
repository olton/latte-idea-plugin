plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.1.0"
  id("org.jetbrains.intellij.platform") version "2.5.0"
  id("org.jetbrains.changelog") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get() 
version = providers.gradleProperty("pluginVersion").get()

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  implementation("com.beust:klaxon:5.5")
  implementation("org.jetbrains:marketplace-zip-signer:0.1.34")

  intellijPlatform {
    webstorm("2025.1")
    bundledPlugin("JavaScript")
  }
}

changelog {
  version.set(providers.gradleProperty("pluginVersion"))
  path.set(file("CHANGELOG.md").canonicalPath)
  header.set(provider { "[${version.get()}]" })
  headerParserRegex.set("""(\d+\.\d+\.\d+)""".toRegex())
  itemPrefix.set("-")
  keepUnreleasedSection.set(true)
  unreleasedTerm.set("[Unreleased]")
  groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "251"
    }

    changeNotes.set(provider {
      changelog.renderItem(
        changelog.getOrNull(providers.gradleProperty("pluginVersion").get()) ?: changelog.getUnreleased()
      )
    })
  }

  signing {
    certificateChain.set(providers.environmentVariable("CERTIFICATE_CHAIN"))
    privateKey.set(providers.environmentVariable("PRIVATE_KEY"))
    password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
  }

  publishing {
    token.set(providers.environmentVariable("PUBLISH_TOKEN"))
  }

  pluginVerification {
    ides {
      recommended()
    }
  }
}

kotlin {
  jvmToolchain(21)
}
