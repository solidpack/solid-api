import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.sonatype.central.portal.publisher)
    `maven-publish`
}

group = "io.github.solidpack"
version = "1.1.6"

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
        name.set("Solid API")
        description.set("A read only API to make working with custom minecraft items/blocks with java edition resource packs easy for developers")
        url.set("https://github.com/solidpack/solid-api")

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
            url.set("https://github.com/solidpack/solid-api")
            connection.set("git:git@github.com:solidpack/solid-api.git")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

