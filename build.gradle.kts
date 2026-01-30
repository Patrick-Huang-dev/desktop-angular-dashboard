/*
 *  Copyright 2025, TeamDev. All rights reserved.
 *
 *  Redistribution and use in source and/or binary forms, with or without
 *  modification, must retain the above copyright notice and the following
 *  disclaimer.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

plugins {
    java
    application
    id("com.teamdev.jxbrowser") version "2.0.0"
}

group = "com.teamdev.jxbrowser.examples"
version = "1.0"

val applicationName = "JxBrowserAngularApp"
val mainJar = "$applicationName-$version.jar"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

jxbrowser {
    version = "8.14.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(jxbrowser.currentPlatform)
    implementation(jxbrowser.swing)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.formdev:flatlaf:3.4")
}

// Configure resources to include Angular build output in production mode
sourceSets {
    main {
        resources {
            // Angular build output will be placed in web-app/dist/web
            // and mapped to /web in the JAR for UrlRequestInterceptor
            srcDir("web-app/dist")
        }
    }
}

val webAppDir = "${projectDir}/web-app"
val isWindows = System.getProperty("os.name").startsWith("Windows")
val npmCommand = if (isWindows) "npm.cmd" else "/usr/bin/env"
val npmArgs = if (isWindows) emptyList<String>() else listOf("npm")

tasks.register<Exec>("npmInstall") {
    workingDir = file(webAppDir)
    commandLine(listOf(npmCommand) + npmArgs + listOf("install"))
}

tasks.register<Exec>("npmBuild") {
    dependsOn("npmInstall")
    workingDir = file(webAppDir)
    commandLine(listOf(npmCommand) + npmArgs + listOf("run", "build"))
    
    // After build, rename the output folder to 'web' for UrlRequestInterceptor
    doLast {
        val distDir = file("$webAppDir/dist")
        val browserDir = file("$webAppDir/dist/angular-jxbrowser-dashboard/browser")
        val webDir = file("$webAppDir/dist/web")
        
        if (browserDir.exists() && !webDir.exists()) {
            browserDir.renameTo(webDir)
            // Clean up the intermediate directory
            file("$webAppDir/dist/angular-jxbrowser-dashboard").deleteRecursively()
        }
    }
}

tasks.register<Exec>("startDevServer") {
    dependsOn("npmInstall")
    workingDir = file(webAppDir)
    commandLine(listOf(npmCommand) + npmArgs + listOf("run", "start"))
}

application {
    mainClass.set("com.teamdev.jxbrowser.angular.App")
    
    // Dev mode enabled by default for `gradle run`
    applicationDefaultJvmArgs = listOf("-Dapp.dev.mode=true")
}

tasks.withType<JavaExec> {
    // Pass all system properties from command line
    systemProperties(System.getProperties().mapKeys { it.key as String })
}

// Configure JAR task to create an executable fat JAR
tasks.jar {
    archiveFileName.set(mainJar)
    dependsOn("npmBuild")
    
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    // Include all runtime dependencies in the JAR (fat JAR)
    from({
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    })
    
    doLast {
        // Copy the JAR to build/dist for jpackage
        copy {
            from("build/libs/$mainJar")
            into("build/dist")
        }
    }
}

// Task to create macOS DMG installer
tasks.register<Exec>("packageDmg") {
    dependsOn(tasks.build)
    
    commandLine(
        "jpackage",
        "--input", "./build/dist",
        "--main-jar", mainJar,
        "--name", applicationName,
        "--app-version", version,
        "--type", "dmg",
        "--main-class", application.mainClass.get(),
        "--dest", "./build/installer"
    )
}

// Task to create Windows EXE installer
tasks.register<Exec>("packageExe") {
    dependsOn(tasks.build)
    
    commandLine(
        "jpackage",
        "--input", "./build/dist",
        "--main-jar", mainJar,
        "--name", applicationName,
        "--app-version", version,
        "--type", "exe",
        "--main-class", application.mainClass.get(),
        "--dest", "./build/installer",
        "--win-dir-chooser",
        "--win-menu",
        "--win-shortcut-prompt"
    )
}






