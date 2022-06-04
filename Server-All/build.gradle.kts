dependencies {
	implementation(project(":Server-All"))
}

tasks.jar {
	from(configurations.runtimeClasspath.get().map {
		if (it.isDirectory) it else zipTree(it)
	})
}
