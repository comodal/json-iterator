buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("pl.allegro.tech.build:axion-release-plugin:+")
    classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:+")
    classpath("org.javamodularity:moduleplugin:+")
    classpath("me.champeau.gradle:jmh-gradle-plugin:+")
  }
}
