plugins {
  `java-library`
  `maven-publish`
  id("com.jfrog.bintray") version "1.8.4" apply false
  id("pl.allegro.tech.build.axion-release") version "1.10.1" apply false
  id("org.javamodularity.moduleplugin") version "1.5.0" apply false
  id("me.champeau.gradle.jmh") version "0.4.8" apply false
  id("io.morethan.jmhreport") version "0.9.0" apply false
}