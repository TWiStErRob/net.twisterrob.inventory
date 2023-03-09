plugins {
	id("org.gradle.java")
	id("org.gradle.jvm-test-suite")
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_7
	targetCompatibility = JavaVersion.VERSION_1_7
}

val integrationTests by tasks.registering {
	// dependsOn("integration*Test") added later.
}

tasks.check.configure {
	dependsOn(integrationTests)
}

@Suppress("UnstableApiUsage")
testing {
	@Suppress("UNUSED_VARIABLE")
	suites {
		named<JvmTestSuite>("test").configure {
			// Keep defaults.
		}
		register<JvmTestSuite>("javaIntegrationTest") {
			testType.set("${TestSuiteType.INTEGRATION_TEST}-java")
			dependencies {
				// None, use default from JRE bootclasspath.
			}
			targets {
				configureEach {
					testTask.configure {
						systemProperty("net.twisterrob.inventory.transform.name", "java")
					}
				}
			}
		}
		register<JvmTestSuite>("xalanIntegrationTest") {
			testType.set("${TestSuiteType.INTEGRATION_TEST}-xalan")
			dependencies {
				implementation(libs.xml.xalan)
			}
			targets {
				configureEach {
					testTask.configure {
						systemProperty("net.twisterrob.inventory.transform.name", "xalan")
					}
				}
			}
		}
		register<JvmTestSuite>("saxonIntegrationTest") {
			testType.set("${TestSuiteType.INTEGRATION_TEST}-saxon")
			dependencies {
				implementation(libs.xml.saxon)
			}
			targets {
				configureEach {
					testTask.configure {
						systemProperty("net.twisterrob.inventory.transform.name", "saxon")
					}
				}
			}
		}
		withType(JvmTestSuite::class)
			.matching { it.testType.get().startsWith("${TestSuiteType.INTEGRATION_TEST}-") }
			.configureEach {
				useJUnit(libs.versions.test.junit4)
				dependencies {
					implementation(project())
				}
				sources {
					java {
						srcDir("src/integrationTest/java")
					}
				}
				targets {
					configureEach {
						integrationTests.configure { dependsOn(testTask) }
						testTask.configure {
							enableAssertions = true
							//val dataXml = file("${rootDir}/../temp/test/data.xml")
							fun Test.inputFile(prop: String, file: File) {
								this.inputs.file(file).withPropertyName(prop).withPathSensitivity(PathSensitivity.RELATIVE)
								this.systemProperty(prop, file)
							}
							inputFile(
								"net.twisterrob.inventory.transform.xml",
								project(":android:backup").file("src/main/assets/demo.xml")
							)
							inputFile(
								"net.twisterrob.inventory.transform.xsd",
								project(":android:backup").file("src/main/assets/data.xml.xsd")
							)
							inputFile(
								"net.twisterrob.inventory.transform.xslt.html",
								project(":android:backup").file("src/main/assets/data.html.xslt")
							)
							inputFile(
								"net.twisterrob.inventory.transform.xslt.csv",
								project(":android:backup").file("src/main/assets/data.csv.xslt")
							)
							val transformDir = file("build/transformed")
							outputs.dir(transformDir).withPropertyName("transformDir")
							systemProperty("net.twisterrob.inventory.transform.output", transformDir)
						}
					}
				}
			}
	}
}
