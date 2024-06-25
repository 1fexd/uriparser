import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.24"
    `java-library`
    `maven-publish`
    id("net.nemerosa.versioning") version "3.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "fe.uribuilder"
version = versioning.info.tag ?: versioning.info.full

repositories {
    mavenCentral()
}

val shadowImplementation = configurations.create("shadowImplementation"){
    configurations.implementation.get().extendsFrom(this)
    isTransitive = false
}

dependencies {
    api(kotlin("stdlib"))
    api(platform("com.github.1fexd:super:0.0.2"))
    api("org.apache.httpcomponents.core5:httpcore5")

    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

val shadowJarTask = tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    exclude("META-INF/**/*")

    archiveClassifier.set("")
    minimize()
    configurations = listOf(shadowImplementation)
}


tasks.named("jar").configure {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            setArtifacts(listOf(shadowJarTask.get()))
            groupId = project.group.toString()
            version = project.version.toString()
        }
    }
}

tasks.whenTaskAdded {
    if (name == "generateMetadataFileForPluginShadowPublication") {
        println(name)
        dependsOn(shadowJarTask)
    }
}
