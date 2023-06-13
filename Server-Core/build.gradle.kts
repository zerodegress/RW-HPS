import java.text.SimpleDateFormat
import java.util.*

//Netty Version
val nettyVersion = "4.1.90.Final"
//Kotlin Version
val kotlinVersion = properties["kotlin.version"]

/**
 * Fuck implementation
 */
dependencies {
	api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

	implementation(project(":TimeTaskQuartz"))
	implementation(project(":ASM-Framework"))

	api("io.netty:netty-buffer:$nettyVersion")
	api("io.netty:netty-codec:$nettyVersion")
	api("io.netty:netty-codec-http:$nettyVersion")
	api("io.netty:netty-handler:$nettyVersion")
	api("io.netty:netty-transport:$nettyVersion")
	api("io.netty:netty-transport-native-epoll:$nettyVersion:linux-aarch_64")
	api("io.netty:netty-transport-native-epoll:$nettyVersion:linux-x86_64")

	compileOnly(fileTree(mapOf("dir" to "libs", "include" to "game-lib.jar")))
	compileOnly(fileTree(mapOf("dir" to "libs", "include" to "slick.jar")))

	api("com.github.deng-rui:RUDP:2.0.0")
	// Json 解析
	// 我建议使用 RW-HPS Json 方法 而不是直接使用依赖
	api("com.google.code.gson:gson:2.10.1")
	api("org.json:json:20230227")

	api("org.apache.commons:commons-compress:1.21")
	api("org.tukaani:xz:1.9")


	api("com.squareup.okhttp3:okhttp:4.10.0") {
		exclude("org.jetbrains.kotlin")
	}
	api("com.vdurmont:emoji-java:5.1.1") {
		exclude("org.json")
	}

	implementation("org.lionsoul:ip2region:1.7.2")

	implementation("net.java.dev.jna:jna:5.13.0")
	implementation("org.jline:jline-reader:3.23.0")
	implementation("org.jline:jline-terminal:3.23.0") {
		exclude("org.jline","jline-native")
	}
	implementation("org.jline:jline-terminal-jna:3.23.0")

	api("it.unimi.dsi:fastutil-core:8.5.12")

	implementation("org.graalvm.js:js:${properties["graalvm.version"]}")

	testApi("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.jar {
	manifest {
		attributes(mapOf("Implementation-Title" to "RW-HPS"))
		attributes(mapOf("Implementation-Vendor" to "RW-HPS Team"))
		attributes(mapOf("Build-Jar-Time" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date().time)))
	}
}

tasks.test {
	useJUnitPlatform()
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = "com.github.RW-HPS"
			artifactId = "Server-Core"
			description = "Dedicated to Rusted Warfare(RustedWarfare) High Performance Server"
			version = "1.0.0"

			from(components.getByName("java"))

			pom {
				scm {
					url.set("https://github.com/RW-HPS/RW-HPS")
					connection.set("scm:https://github.com/RW-HPS/RW-HPS.git")
					developerConnection.set("scm:git@github.com:RW-HPS/RW-HPS.git")
				}

				licenses {
					license {
						name.set("GNU AGPLv3")
						url.set("https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE")
					}
				}

				developers {
					developer {
						id.set("RW-HPS")
						name.set("RW-HPS Technologies")
					}
				}

			}

			pom.withXml {
				val root = asNode()
				root.appendNode("description", project.description)
				root.appendNode("name", project.name)
				root.appendNode("url", "https://github.com/RW-HPS/RW-HPS")
			}
		}
	}
}