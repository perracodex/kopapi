/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.configurationcache.extensions.capitalized

plugins {
    `java-library`
    signing
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.vanniktech)
}

group = project.properties["group"] as String
version = project.properties["version"] as String

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvmToolchain(jdkVersion = 17)

    // Enable explicit API mode.
    // https://github.com/Kotlin/KEEP/blob/master/proposals/explicit-api-mode.md
    // https://kotlinlang.org/docs/whatsnew14.html#explicit-api-mode-for-library-authors
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-opt-in=kotlin.ExperimentalStdlibApi")
        freeCompilerArgs.add("-opt-in=kotlin.ExperimentalUnsignedTypes")
    }
}

dependencies {
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.yaml)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.html.builder)

    implementation(libs.swagger.parser)
    implementation(libs.swagger.ui)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.kotlin.junit)
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = 1
}


// https://central.sonatype.com/account
// https://central.sonatype.com/publishing/deployments
// https://vanniktech.github.io/gradle-maven-publish-plugin/central/#automatic-release
mavenPublishing {
    val artifactId: String = project.properties["artifactId"] as String
    val repository: String = project.properties["repository"] as String
    val repositoryConnection: String = project.properties["repositoryConnection"] as String
    val developer: String = project.properties["developer"] as String
    val pomName: String = project.properties["pomName"] as String
    val pomDescription: String = project.properties["pomDescription"] as String

    coordinates(
        groupId = group as String,
        artifactId = artifactId,
        version = version as String
    )

    pom {
        name.set(pomName)
        description.set(pomDescription)
        url.set("https://$repository/$artifactId")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set(developer)
                name.set(developer.capitalized())
                email.set(System.getenv("DEVELOPER_EMAIL"))
                url = "https://$repository"
            }
        }
        scm {
            connection.set("scm:git:git://$repository/$artifactId.git")
            developerConnection.set("scm:git:ssh://$repositoryConnection/$artifactId.git")
            url.set("https://$repository/$artifactId")
        }
    }

    publishToMavenCentral(host = SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
    signAllPublications()
}

signing {
    val privateKeyPath: String? = System.getenv("MAVEN_SIGNING_KEY_PATH")
    val passphrase: String? = System.getenv("MAVEN_SIGNING_KEY_PASSPHRASE")

    if (privateKeyPath.isNullOrBlank())
        println("MAVEN_SIGNING_KEY_PATH is not set. Skipping signing.")
    else if (passphrase.isNullOrBlank()) {
        println("MAVEN_SIGNING_KEY_PASSPHRASE is not set. Skipping signing.")
    } else {
        val privateKey: String = File(privateKeyPath).readText()
        useInMemoryPgpKeys(privateKey, passphrase)
        sign(publishing.publications)
    }
}
