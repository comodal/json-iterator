plugins {
  id("software.sava.gradle.feature.publish-maven-central")
}

dependencies {
  nmcpAggregation(project(":json-iterator"))
}
