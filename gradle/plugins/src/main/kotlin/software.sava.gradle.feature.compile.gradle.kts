plugins {
  id("java")
}

val jlv = JavaLanguageVersion.of(providers.gradleProperty("javaVersion").getOrElse("21"))

java {
  toolchain.languageVersion = jlv
}
