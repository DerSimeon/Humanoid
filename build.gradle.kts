/*
 * MIT License
 *
 * Copyright (c) 2025 Simeon L.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import xyz.jpenilla.gremlin.gradle.WriteDependencySet

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.shadow)
    alias(libs.plugins.runtask)
    alias(libs.plugins.resourcefactory)
    alias(libs.plugins.gremlin)
    alias(libs.plugins.userdev)
}

group = "lol.simeon"
version = "1.0-SNAPSHOT"
description = "Yet another NPC Plugin"

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.triumphteam.dev/snapshots/")
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    runtimeDownload(libs.polaris)
    runtimeDownload(libs.triumph.cmds)
    runtimeDownload(libs.triumph.coroutines)
    runtimeDownload(libs.triumph.extensions)
    runtimeDownload(libs.jansi)
    runtimeDownload(libs.stdlib)
    runtimeDownload(libs.coroutines)
}

kotlin {
    jvmToolchain(21)
    explicitApi()
}


paperPluginYaml {
    apiVersion = "1.21"
    author = "Simeon"
    main = "lol.simeon.humanoid.Humanoid"
    bootstrapper = "lol.simeon.humanoid.boot.HumanoidPluginBootstrapper"
    loader = "lol.simeon.humanoid.boot.HumanoidPluginLoader"
}

tasks.withType<WriteDependencySet> {
    outputFileName.set("humanoid-dependencies.txt")
    repos.add("https://repo.papermc.io/repository/maven-public/")
    repos.add("https://repo.maven.apache.org/maven2/")
    repos.add("https://repo.triumphteam.dev/snapshots/")
}

gremlin {
    defaultJarRelocatorDependencies.set(false)
    defaultGremlinRuntimeDependency.set(true)
}

configurations.compileOnly {
    extendsFrom(configurations.runtimeDownload.get())
}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}

dokka {
    moduleName.set("Humanoid")
    dokkaSourceSets.main {
        sourceRoots.setFrom("src/main/kotlin/lol/simeon/humanoid/api")
        skipDeprecated.set(false)
        reportUndocumented.set(false)
    }
    pluginsConfiguration.html {
        footerMessage.set("(c) 2025 Simeon L.")
    }
}
