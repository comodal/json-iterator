plugins {
  id("java")
  id("maven-publish")
  id("signing")
  id("com.gradleup.nmcp")
}

val gprUser =
  providers.gradleProperty("gpr.user.write").orElse(providers.environmentVariable("GITHUB_ACTOR")).orElse("")
val gprToken =
  providers.gradleProperty("gpr.token.write").orElse(providers.environmentVariable("GITHUB_TOKEN")).orElse("")
val signingKey =
  providers.environmentVariable("GPG_PUBLISH_SECRET").orNull
val signingPassphrase =
  providers.environmentVariable("GPG_PUBLISH_PHRASE").orNull
val publishSigningEnabled =
  providers.gradleProperty("sign").getOrElse("false").toBoolean()

java {
  withJavadocJar()
  withSourcesJar()
}

signing {
  sign(publishing.publications)
  useInMemoryPgpKeys(signingKey, signingPassphrase)
}

tasks.withType<Sign>().configureEach { enabled = publishSigningEnabled }

val vcs = "https://github.com/sava-software/json-iterator"

publishing {
  publications.withType<MavenPublication>().configureEach {
    versionMapping {
      allVariants { fromResolutionResult() }
    }
  }

  publications.register<MavenPublication>("mavenJava") {
    from(components["java"])

    pom {
      name = project.name
      description = "Json Parser"
      url = vcs
      licenses {
        license {
          name = "MIT License"
          url = "https://github.com/sava-software/json-iterator/blob/master/LICENSE"
        }
      }
      developers {
        developer {
          name = "Jim"
          id = "jpe7s"
          email = "jpe7s.salt188@passfwd.com"
          organization = "Sava Software"
          organizationUrl = "https://github.com/sava-software"
        }
      }
      scm {
        connection = "scm:git:git@github.com:sava-software/json-iterator.git"
        developerConnection = "scm:git:ssh@github.com:sava-software/json-iterator.git"
        url = vcs
      }
    }
  }

  repositories {
    maven {
      name = "GithubPackages"
      url = uri("https://maven.pkg.github.com/sava-software/json-iterator")
      credentials {
        username = gprUser.get()
        password = gprToken.get()
      }
    }
  }
}
