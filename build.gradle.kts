plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("multiplatform").apply(false)
    id("com.android.application").apply(false)
    id("com.android.library").apply(false)
    id("org.jetbrains.compose").apply(false)
    id("org.jetbrains.kotlin.plugin.compose").apply(false)
    id("com.diffplug.spotless")
    id("com.vanniktech.maven.publish")
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    spotless {
        val ktlintVersion = "0.49.1"
        kotlin {
            target("src/**/*.kt")
            targetExclude("**/build/")
            ktlint(ktlintVersion)
        }
        kotlinGradle {
            target("*.kts")
            targetExclude("**/build/")
            ktlint(ktlintVersion)
        }
    }
    group = "media.kamel"
    version = "0.0.1"
    afterEvaluate {
        extensions.findByType<PublishingExtension>()?.apply {
            publications.withType<MavenPublication>().configureEach {
                pom {
                    name.set("avif-kmp")
                    description.set("Kotlin Multi Platform bindings for AOMediaCodec libavif library.")
                    url.set("https://github.com/qdsfdhvh/avif-kmp")
                    licenses {
                        license {
                            name.set("The Apache License 2.0")
                            url.set("https://opensource.org/licenses/Apache-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("Seiko")
                            name.set("SeikoDes")
                            email.set("seiko_des@outlook.com")
                        }
                    }
                    scm {
                        url.set("https://github.com/qdsfdhvh/avif-kmp")
                        connection.set("scm:git:git://github.com/qdsfdhvh/avif-kmp.git")
                        developerConnection.set("scm:git:git://github.com/qdsfdhvh/avif-kmp.git")
                    }
                }
            }
        }
    }
}
