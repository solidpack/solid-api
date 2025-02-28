import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.sonatype.central.portal.publisher)
    `maven-publish`
}

group = "io.github.solid-resourcepack"
version = "1.1.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    testImplementation(rootProject.libs.kotlin.test)
    implementation(rootProject.libs.kotlin.jvm)
    implementation(rootProject.libs.bundles.configurate)
    api(rootProject.libs.adventure)
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

tasks.named("shadowJar", ShadowJar::class) {
    mergeServiceFiles()
    archiveFileName.set("${project.name}.jar")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

centralPortal {
    name = project.name

    username = project.findProperty("sonatypeUsername") as? String
    password = project.findProperty("sonatypePassword") as? String

    pom {
        name.set("Solid")
        description.set("An API wrapper around unnamed/creative to make custom minecraft items/blocks with java edition resource packs easy for developers")
        url.set("https://github.com/solid-resourcepack/solid")

        developers {
            developer {
                id.set("dayyeeet")
                email.set("david@cappell.net")
            }
        }
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        scm {
            url.set("https://github.com/solid-resourcepack/solid")
            connection.set("git:git@github.com:solid-resourcepack/solid.git")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

