import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "com.dimdarkevil"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.xmlgraphics:batik-transcoder:1.14")
    implementation("org.apache.xmlgraphics:batik-codec:1.14")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")
    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("com.github.oshi:oshi-core:6.1.4")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    archiveFileName.set("${project.name}.jar")
}

tasks.withType<ProcessResources> {
    eachFile {
        if (name == "version.properties") {
            filter { line ->
                line.replace("%project.version%", "${project.version}")
            }
        }
    }
}

application {
    mainClass.set("com.dimdarkevil.noitautil.KotlinMain")
    applicationName = "noita-util"
}

distributions {
    main {
        distributionBaseName.set(project.name)
        contents {
            from("README.md")
        }
    }
}
