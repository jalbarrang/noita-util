import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    application
    id("edu.sc.seis.launch4j") version "2.5.3"
}

group = "com.dimdarkevil"
version = "1.1.0"

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
}

tasks.withType<edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask> {
    outfile = "${productName}.exe"
    mainClassName = application.mainClass.get()
    icon = "${projectDir}/src/main/resources/noita-util.ico"
    productName = productName
}
