import org.gradle.api.internal.plugins.StartScriptGenerator
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    application
    java
}

val jre: Configuration by configurations.creating
val zuluVersion = "17.0.2_8"
val x64Windows = "x64_windows"
val x86Windows = "x86-32_windows"
val x64Linux = "x64_linux"
val aarch64Linux = "aarch64_linux"
val x64Mac = "x64_mac"
val aarch64Mac = "aarch64_mac"

group = "com.dimdarkevil"
version = "1.2.1"

repositories {
    mavenCentral()

    val zulu = ivy {
        url = uri("https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.2%2B8")
        patternLayout {
            // https://corretto.aws/[latest/latest_checksum]/amazon-corretto-[corretto_version]-[cpu_arch]-[os]-[package_type].[file_extension]
            //artifact("/[organisation]-[module]-[revision]-[classifier].[ext]")
            artifact("/[organization]_[module]_[revision].[ext]")
        }
        // This is required in Gradle 6.0+ as metadata file (ivy.xml) is mandatory.
        // https://docs.gradle.org/6.2/userguide/declaring_repositories.html#sec:supported_metadata_sources
        metadataSources { artifact() }
    }
    // Use correto only for amazon dependencies
    // https://docs.gradle.org/current/userguide/declaring_repositories.html#declaring_content_exclusively_found_in_one_repository
    exclusiveContent {
        forRepositories(zulu)
        filter { includeGroup("OpenJDK17U-jre") }
    }
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("com.github.oshi:oshi-core:6.1.5")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    testImplementation(kotlin("test"))

    // For the zulu ivy repo, the dependency maps to the pattern:
    // [module]_[artifact]_[revision].[ext]
    // In maven term: [group]:[artifact]:[version]:[classifier]@[ext]
    jre("OpenJDK17U-jre:${x64Windows}_hotspot:${zuluVersion}@zip")
    jre("OpenJDK17U-jre:${x86Windows}_hotspot:${zuluVersion}@zip")
    jre("OpenJDK17U-jre:${x64Linux}_hotspot:${zuluVersion}@tar.gz")
    jre("OpenJDK17U-jre:${aarch64Linux}_hotspot:${zuluVersion}@tar.gz")
    jre("OpenJDK17U-jre:${x64Mac}_hotspot:${zuluVersion}@tar.gz")
    jre("OpenJDK17U-jre:${aarch64Mac}_hotspot:${zuluVersion}@tar.gz")
}

val archiveFileName = tasks.withType<Jar>().first()?.archiveFileName
    ?: throw RuntimeException("archiveFileName not set")

tasks.withType<CreateStartScripts> {
    val unixTemplate = resources.text.fromFile(File(projectDir, "startscripts/unix.txt"))
    val windowsTemplate = resources.text.fromFile(File(projectDir, "startscripts/windows.txt"))
    (unixStartScriptGenerator as TemplateBasedScriptGenerator).template = unixTemplate
    (windowsStartScriptGenerator as TemplateBasedScriptGenerator).template = windowsTemplate
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

abstract class JreTask @Inject constructor(
    private val jre: Configuration,
    private val osTarget: String
) : DefaultTask() {
    @TaskAction
    fun copyJvm() {
        project.copy {
            // Listing
            // project.configurations.compileClasspath.get().files.map { key -> println(key.name) };
            val zulu = Regex("${osTarget}.*")
            println("-=-= osTarget: ${osTarget}")
            project.configurations[jre.name].forEach {
                println("-=-= file: ${it.canonicalPath}")
            }
            val jvmZipFile = project.configurations[jre.name].resolve().find { file -> file.name.matches(zulu) }
            println("$osTarget Jvm file to unzip: $jvmZipFile")
            // Unarchive doc https://docs.gradle.org/current/userguide/working_with_files.html#sec:unpacking_archives_example
            if (jvmZipFile == null) {
                throw RuntimeException("Unable to find a Jvm file dependency for the OS (${osTarget})")
            }
            val fileTree: FileTree = when (jvmZipFile.extension) {
                "zip" -> project.zipTree(jvmZipFile)
                "gz" -> project.tarTree(jvmZipFile)
                else -> null
            } ?: throw RuntimeException("The extension (${jvmZipFile.extension}) is unknown for the Jvm file")
            from(fileTree) {
                eachFile {
                // delete the first directory as explained in the example 12
                // https://docs.gradle.org/current/userguide/working_with_files.html#sec:unpacking_archives_example
                    var segments = relativePath.segments.drop(1)
                    // macos has more than a root directory:
                    //   * the home is at amazon-correto-8/Contents/Home
                    //   * and there is third directory such as Contents/MacOS
                    if (osTarget == "macos") {
                        if (segments.size >= 2 && segments[0] == "Contents" && segments[1] == "Home") {
                            segments = segments.drop(2)
                        } else {
                            this.exclude()
                        }
                    }
                    relativePath = RelativePath(true, *segments.toTypedArray())
                }
            }
            into(project.layout.buildDirectory.dir("jre/${osTarget}"))
            println("${osTarget} jre file unzipped")
        }
    }
}

tasks.register<JreTask>("${x64Windows}Jre", jre, x64Windows)
tasks.register<JreTask>("${x86Windows}Jre", jre, x86Windows)
tasks.register<JreTask>("${x64Linux}Jre", jre, x64Linux)
tasks.register<JreTask>("${aarch64Linux}Jre", jre, aarch64Linux)
tasks.register<JreTask>("${x64Mac}Jre", jre, x64Mac)
tasks.register<JreTask>("${aarch64Mac}Jre", jre, aarch64Mac)

distributions {
    main {
        contents {
            duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
            into("bin") {
                from("${buildDir}/scripts") {
                    fileMode = Integer.parseInt("755", 8)
                } // or 493 which is 755 base 8 in base 10
            }
            // Copy the jar of the cli (ie the actual project artifact generated)
            into("lib") {
                from(project.layout.buildDirectory.dir("libs").get().file(archiveFileName))
            }
            // Copy the runtime dependencies
            into("lib") { //
                from(configurations.runtimeClasspath)
            }
        }
    }
    create(x64Windows) {
        contents {
            into("jre") { from("${buildDir}/jre/$x64Windows") }
            with(distributions.main.get().contents)
        }
    }
    create(x86Windows) {
        contents {
            into("jre") { from("${buildDir}/jre/$x86Windows") }
            with(distributions.main.get().contents)
        }
    }
    create(x64Linux) {
        contents {
            into("jre") { from("${buildDir}/jre/$x64Linux") }
            with(distributions.main.get().contents)
        }
    }
    create(aarch64Linux) {
        contents {
            into("jre") { from("${buildDir}/jre/$aarch64Linux") }
            with(distributions.main.get().contents)
        }
    }
    create(x64Mac) {
        contents {
            into("jre") { from("${buildDir}/jre/$x64Mac") }
            with(distributions.main.get().contents)
        }
    }
    create(aarch64Mac) {
        contents {
            into("jre") { from("${buildDir}/jre/$aarch64Mac") }
            with(distributions.main.get().contents)
        }
    }
}


tasks.getByName("${x86Windows}DistZip")
    .dependsOn("${x86Windows}Jre")

tasks.getByName("${x64Windows}DistZip")
    .dependsOn("${x64Windows}Jre")

tasks.getByName("${x64Linux}DistZip")
    .dependsOn("${x64Linux}Jre")

tasks.getByName("${aarch64Linux}DistZip")
    .dependsOn("${aarch64Linux}Jre")

tasks.getByName("${x64Mac}DistZip")
    .dependsOn("${x64Mac}Jre")

tasks.getByName("${aarch64Mac}DistZip")
    .dependsOn("${aarch64Mac}Jre")

val allDistZip = tasks.register("allDistZip").get()
    .dependsOn(tasks.getByName("distZip"))
    .dependsOn("${x86Windows}DistZip")
    .dependsOn("${x64Windows}DistZip")
    .dependsOn("${x64Linux}DistZip")
    .dependsOn("${aarch64Linux}DistZip")
    .dependsOn("${x64Mac}DistZip")
    .dependsOn("${aarch64Mac}DistZip")
