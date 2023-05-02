dependencies {
	implementation("org.ow2.asm:asm:9.4")
	implementation("org.ow2.asm:asm-tree:9.4")
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = "com.github.RW-HPS"
			artifactId = "ASM-Framework"
			description = "Dedicated to Rusted Warfare(RustedWarfare) High Performance Server"

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