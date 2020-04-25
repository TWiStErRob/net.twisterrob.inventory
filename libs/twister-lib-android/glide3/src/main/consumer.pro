# See net.twisterrob.android.content.glide.SVGWorkarounds.getFromInputStream
-keepnames class com.caverock.androidsvg.SVGParser
-keepnames class com.caverock.androidsvg.SVGParser$SAXHandler

# Note: net.twisterrob.android.content.glide.SVGWorkarounds accesses a declared field 'svgDocument' dynamically
#       Maybe this is program field 'com.caverock.androidsvg.SVGParser { com.caverock.androidsvg.SVG svgDocument; }'
-dontnote net.twisterrob.android.content.glide.SVGWorkarounds
-keepclassmembernames class com.caverock.androidsvg.SVGParser {
	com.caverock.androidsvg.SVG svgDocument;
}
