import org.gradle.kotlin.dsl.project
import java.text.SimpleDateFormat
import java.util.Date

dependencies {
	implementation(project(":Server-Core"))
}

tasks.jar {
	// Fuck Java 9
	exclude("META-INF/version/**")
	exclude("**/module-info.class")
	// Clean Import
	exclude("META-INF/maven/**")
	exclude("META-INF/AL2.0")
	exclude("META-INF/LGPL2.1")
	exclude("META-INF/native-image/**")
	// Clean Proguard
	exclude("META-INF/proguard/**")
	// Clean Kotlin
	exclude("META-INF/**.kotlin_module")

	exclude("META-INF/**/LICENSE.txt")
	exclude("META-INF/LICENSE")
	exclude("META-INF/NOTICE.txt")
	exclude("META-INF/NOTICE")

	// Fuck Netty !!!!!!
	exclude("META-INF/INDEX.LIST")
	exclude("META-INF/**.properties")
	exclude("META-INF/**.xml")

	exclude("META-INF/DEPENDENCIES")
	exclude("META-INF/sisu/javax.inject.Named")
	exclude("about.html")

	exclude("META-INF/LWJGL.*")

	exclude("META-INF/BC2048KE.SF")
	exclude("META-INF/BC2048KE.DSA")
	exclude("META-INF/BC1024KE.SF")
	exclude("META-INF/BC1024KE.DSA")

	duplicatesStrategy = DuplicatesStrategy.EXCLUDE

	manifest {
		attributes(mapOf("Main-Class" to "net.rwhps.server.Main"))
		attributes(mapOf("Launcher-Agent-Class" to  "net.rwhps.server.dependent.AgentAttachData"))
		attributes(mapOf("Can-Redefine-Classes" to  "true"))

		attributes(mapOf("Implementation-Title" to "RW-HPS"))
		attributes(mapOf("Implementation-Vendor" to "RW-HPS Team"))

		attributes(mapOf("Build-Jar-Time" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date().time)))
	}

	from(configurations.runtimeClasspath.get().map {
		if (it.isDirectory) it else zipTree(it)
	})

}