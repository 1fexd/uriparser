plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.8.21"
    id("net.nemerosa.versioning") version "3.0.0"
}

group = "fe.uribuilder"
version = versioning.info.tag ?: versioning.info.full

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            version = project.version.toString()

            from(components["java"])
        }
    }
}
