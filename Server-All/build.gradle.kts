dependencies {
	implementation(project(":Server-Core"))
}

tasks.jar {
	from(configurations.runtimeClasspath.get().map {
		if (it.isDirectory) it else zipTree(it)
	})
}
