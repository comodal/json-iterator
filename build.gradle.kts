plugins {
  `java-library`
  `maven-publish`
  id("com.jfrog.bintray") version "1.8.4" apply false
  id("pl.allegro.tech.build.axion-release") version "1.10.2" apply false
  id("org.javamodularity.moduleplugin") version "1.6.0" apply false
  id("me.champeau.gradle.jmh") version "0.5.0-rc-2" apply false
  id("io.morethan.jmhreport") version "0.9.0" apply false
  id("io.freefair.github.package-registry-maven-publish") version "4.1.0" apply false
}