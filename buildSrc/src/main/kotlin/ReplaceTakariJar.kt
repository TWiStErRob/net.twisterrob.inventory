import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.artifacts.maven.PomModuleDescriptor
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.internal.artifacts.repositories.resolver.VariantMetadataAdapter
import org.gradle.internal.component.external.model.maven.DefaultMutableMavenModuleResolveMetadata
import org.gradle.internal.component.external.model.maven.MavenModuleResolveMetadata
import org.gradle.kotlin.dsl.getDescriptor

/**
 * See `<packaging>takari-jar</packaging>` in
 * https://repo1.maven.org/maven2/io/takari/junit/takari-cpsuite/1.2.7/takari-cpsuite-1.2.7.pom
 * 
 * Fixes
 * ```
 * Could not resolve all task dependencies for configuration ':android:debugAndroidTestRuntimeClasspath'.
 *   > No variants of io.takari.junit:takari-cpsuite:1.2.7 match the consumer attributes:
 *     - io.takari.junit:takari-cpsuite:1.2.7 configuration runtime declares a runtime of a component:
 *       - Incompatible because this component declares a component, as well as attribute 'artifactType' with value 'takari-jar' and the consumer needed a component, as well as attribute 'artifactType' with value
 *   'android-classes-jar'
 *       - Other compatible attributes:
 *         - Doesn't say anything about com.android.build.api.attributes.BuildTypeAttr (required 'debug')
 *         - Doesn't say anything about dexing-enable-desugaring (required 'true')
 *         - Doesn't say anything about dexing-incremental-transform (required 'false')
 *         - Doesn't say anything about dexing-is-debuggable (required 'true')
 *         - Doesn't say anything about dexing-min-sdk (required '21')
 * ```
 * when used as
 * ```
 * dependencies.components {
 *   withModule("io.takari.junit:takari-cpsuite", ReplaceTakariJar)
 * }
 * ```
 */
abstract class ReplaceTakariJar : ComponentMetadataRule {

	override fun execute(context: ComponentMetadataContext) {
		context.getDescriptor(PomModuleDescriptor::class.java)?.let {
			println(it.packaging)
		}
		context.details.allVariants { 
			println("${this}: ${this.attributes.keySet().map { it to this.attributes.getAttribute(it)}}")
			this.withDependencyConstraints { 
				this.forEach { dd->
					println("${dd} (${dd.module}): ${dd.attributes.keySet().map { it to dd.attributes.getAttribute(it)}}")
				}
			}
			this.withCapabilities { 
				println(this.capabilities.map { "${it.group}:${it.name}:${it.version}" })
			}
			this.withDependencies { 
				this.forEach { dd -> 
					println("${dd} (${dd.module}): ${dd.attributes.keySet().map { it to dd.attributes.getAttribute(it)}}")
				}
			}
		}
		context.details.withVariant("runtime") {
			val metadata = VariantMetadataAdapter::class.java
				.getDeclaredField("metadata")
				.apply { isAccessible = true }
				.get(this) as DefaultMutableMavenModuleResolveMetadata
			
			metadata.packaging = "jar"
			println("${this}: ${this.attributes.keySet().map { it to this.attributes.getAttribute(it)}}")
			this.withDependencyConstraints {
				this.forEach { dd->
					println("${dd} (${dd.module}): ${dd.attributes.keySet().map { it to dd.attributes.getAttribute(it)}}")
				}
			}
			this.withCapabilities {
				println(this.capabilities.map { "${it.group}:${it.name}:${it.version}" })
			}
			this.withDependencies {
				this.forEach { dd ->
					println("${dd} (${dd.module}): ${dd.attributes.keySet().map { it to dd.attributes.getAttribute(it)}}")
				}
			}
			attributes {
//				val original = getAttribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE)!!
//				if (original == "takari-jar") {
//					attribute(
//						ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE,
//						ArtifactTypeDefinition.JAR_TYPE
//					)
//				} else {
//					error("It is no longer necessary to override artifact type for ${context.details.id}")
//				}
			}
		}
	}
}
