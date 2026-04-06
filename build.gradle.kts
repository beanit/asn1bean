import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    `maven-publish`
    signing
    eclipse
    id("net.ltgt.errorprone") version "5.1.0"
    id("com.diffplug.spotless") version "8.4.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

var cfgJavaVersion = JavaVersion.VERSION_21

val sonatypeRepository: String? by project
val sonatypeSnapshotRepository: String? by project
val sonatypeUser: String? by project
val sonatypePass: String? by project
val sonatypeStagingProfileId: String? by project

//----------- project specific configuration start --------------------

val cfgVersion = "1.15.0"
val cfgGroup = "com.beanit"
val cfgCopyToRoot = true
val cfgSignPom = true
val javaProjects: Set<Project> = subprojects
val distributionProjects = javaProjects
val docProjects = distributionProjects.filter { it.path == ":asn1bean" }
val repositoryProjects = javaProjects
val cfgModuleName = "com.beanit.asn1bean"

tasks.register<Tar>("tar") {
    into(project.name) {
        from("./") {
            include("build.gradle.kts")
            include("settings.gradle.kts")
            include("LICENSE.txt")
            include("doc/**")
            include("bin/**")
            include("gradle/wrapper/**")
            include("gradlew")
            include("gradlew.bat")
            include("build/libs-all/**")
            include("src/**")

            include("projects/asn1bean/src/**")
            include("projects/asn1bean/build.gradle.kts")

            include("projects/asn1bean-compiler/src/**")
            include("projects/asn1bean-compiler/build.gradle.kts")
            include("projects/asn1bean-compiler/antlr/**")
            include("projects/asn1bean-compiler/dependencies/**")
        }
    }

    into(project.name + "/doc/") {
        from("projects/asn1bean/build/docs/") {
            include("javadoc/**")
        }
    }
}


//----------- project specific configuration end ---------------------


configure(allprojects) {
    version = cfgVersion
    group = cfgGroup
}



configure(javaProjects) {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "eclipse")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "net.ltgt.errorprone")

    tasks.publish {
        enabled = false
    }

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = cfgJavaVersion
        targetCompatibility = cfgJavaVersion
        withSourcesJar()
        withJavadocJar()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.1")
        errorprone("com.google.errorprone:error_prone_core:2.48.0")
    }

    tasks.test {
        useJUnitPlatform()
        systemProperty("junit.jupiter.execution.parallel.enabled", "true")
        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.errorprone.excludedPaths.set(".*java-gen.*")
    }

    afterEvaluate {
        tasks.jar {
            manifest {
                attributes["Automatic-Module-Name"] = project.extra["cfgModuleName"]
            }
        }
    }

    spotless {
        java {
            googleJavaFormat()
        }
    }

    tasks.register<Copy>("jarAll") {
        from(configurations.runtimeClasspath) // all runtime dependencies
        from(tasks.jar) // the jar file created
        if (cfgCopyToRoot) {
            into(rootDir.path + "/build/libs-all")
        } else {
            into("build/libs-all")
        }
    }

    tasks.build {
        dependsOn("jarAll")
    }

    eclipse.pathVariables(mapOf("GRADLE_USER_HOME" to file(gradle.gradleUserHomeDir)))
    tasks.eclipse { dependsOn(tasks.cleanEclipse) }

    tasks.javadoc {
        exclude("**/internal/**")
        exclude("**/java-gen/**")
        exclude("**/app/**")

        //linking Javadoc in version prior 9 does not work well because Javadoc uses html frames.
        if (cfgJavaVersion.isJava9Compatible) {
            if (cfgJavaVersion.isJava11Compatible) {
                (options as StandardJavadocDocletOptions).links?.add("https://docs.oracle.com/en/java/javase/${cfgJavaVersion.majorVersion}/docs/api/")
            } else {
                (options as StandardJavadocDocletOptions).links?.add("https://docs.oracle.com/javase/${cfgJavaVersion.majorVersion}/docs/api/")
            }
        }
    }
}

configure(repositoryProjects) {
    tasks.publish {
        enabled = true
    }
}

configure(repositoryProjects) {
    mavenPublishing {
        publishToMavenCentral()
        signAllPublications()

        pom {
            inceptionYear.set("2011")
            url.set("https://www.beanit.com/asn1/")
            developers {
                developer {
                    id.set("beanit")
                    name.set("Beanit GmbH")
                }
            }
            scm {
                url.set("https://github.com/beanit/asn1bean")
                connection.set("scm:git:git://github.com/beanit/asn1bean")
            }
        }
    }
}


tasks.register<Javadoc>("javadocAll") {

    source(docProjects.map { project ->
        project.sourceSets["main"].allJava
    })

    exclude("**/internal/**")
    exclude("**/java-gen/**")
    exclude("**/app/**")

    setDestinationDir(layout.buildDirectory.dir("docs/javadoc-all").get().asFile)

    classpath = files(distributionProjects.map { project ->
        project.sourceSets["main"].compileClasspath
    })

    classpath += files(distributionProjects.map { project ->
        project.sourceSets["main"].output
    })

    //linking Javadoc in version prior 9 does not work well because Javadoc uses html frames.
    if (cfgJavaVersion.isJava9Compatible) {
        if (cfgJavaVersion.isJava11Compatible) {
            (options as StandardJavadocDocletOptions).links?.add("https://docs.oracle.com/en/java/javase/${cfgJavaVersion.majorVersion}/docs/api/")
        } else {
            (options as StandardJavadocDocletOptions).links?.add("https://docs.oracle.com/javase/${cfgJavaVersion.majorVersion}/docs/api/")
        }
    }

}

tasks.named<Tar>("tar") {
    archiveFileName.set(project.name + "-" + project.version + ".tgz")

    dependsOn(distributionProjects.map { "${it.path}:build" })
    distributionProjects.forEach {
        println("project: "
                + it.path)
    }
    dependsOn(tasks.named("javadocAll"))

    compression = Compression.GZIP

    destinationDirectory.set(File("build/distributions/"))
}
