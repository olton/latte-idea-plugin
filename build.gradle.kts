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

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "251"
    }

    changeNotes = """
        <h2>Fixes</h2>
        <ul>
            <li>Fixed issue #1</li>
            <li>General improves</li>
        </ul>
    """.trimIndent()
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
