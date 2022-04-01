import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20-M1"
    application
    id("org.beryx.jlink") version "2.25.0"
}

group = "com.dimdarkevil"
version = "1.0.3"

val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
compileJava.destinationDir = compileKotlin.destinationDir

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("com.github.oshi:oshi-core:6.1.5")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

/*
tasks.withType<Jar> {
    archiveFileName.set("${project.name}.jar")
}
*/

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
    mainClassName = "com.dimdarkevil.noitautil.KotlinMain"
    mainModule.set("noitautil")
}

/*
distributions {
    main {
        distributionBaseName.set(project.name)
        contents {
            from("README.md")
        }
    }
}
*/

jlink {
    val currentOs = org.gradle.internal.os.OperatingSystem.current()
    forceMerge("kotlin")
    launcher {
        name = "noita-util"
    }
    imageZip.set(project.file("${project.buildDir}/image-zip/noita-util-image-${project.version}.zip"))
    jpackage {
        if (currentOs.isWindows) {
            installerOptions = installerOptions.plus(listOf(
                "--win-per-user-install", "--win-dir-chooser", "--win-menu", "--win-shortcut"
            ))
        }
    }
}