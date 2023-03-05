@SuppressWarnings("UnnecessaryQualifiedReference")
class Mapping {
	private static java.util.regex.Pattern MAPPING_PATTERN =
			~/^(?<member>    )?(?<location>\d+:\d+:)?(?:(?<type>.*?) )?(?<name>.*?)(?:\((?<args>.*?)\))?(?: -> )(?<obfuscated>.*?)(?<class>:?)$/ 
	private static int MAPPING_PATTERN_OBFUSCATED_INDEX = 6

	private final File source
	Mapping(File source) {
		this.source = source
	}

	void remap(File target) {
		target.withWriter { source.eachLine Mapping.&processLine.curry(it) }
	}

	// CONSIDER supporting -applymapping by using the names from the original -applymapping file
	private static void processLine(Writer out, String line, int num) {
		java.util.regex.Matcher m = MAPPING_PATTERN.matcher(line)
		if (!m.find()) {
			throw new IllegalArgumentException("Line #${num} is not recognized: ${line}")
		}
		try {
			def originalName = m.group("name")
			def obfuscatedName = m.group("obfuscated")
			def newName = originalName.equals(obfuscatedName) ? obfuscatedName : unfuscate(originalName, obfuscatedName)
			out.write(line.substring(0, m.start(MAPPING_PATTERN_OBFUSCATED_INDEX)))
			out.write(newName)
			out.write(line.substring(m.end(MAPPING_PATTERN_OBFUSCATED_INDEX)))
			out.write('\n')
		} catch (Exception ex) {
			StringBuilder sb = new StringBuilder("Line #${num} failed: ${line}\n")
			0.upto(m.groupCount()) { Number it -> sb.append("Group #${it}: '${m.group(it.intValue())}'\n") }
			throw new IllegalArgumentException(sb.toString(), ex)
		}
	}

	private static String unfuscate(String original, String obfuscated) {
		// reassemble the names with something readable, but still breaking changes
		def origName = getName(original)
		def obfName = getName(obfuscated)
		obfName = obfName.equals(origName) ? "" : obfName
		return getPackage(original) + origName + '_' + obfName
	}
	private static String getPackage(String name) {
		int lastDot = name.lastIndexOf('.')
		return lastDot < 0 ? "" : name.substring(0, lastDot + 1)
	}
	private static String getName(String name) {
		return name.substring(name.lastIndexOf('.') + 1)
	}
}
