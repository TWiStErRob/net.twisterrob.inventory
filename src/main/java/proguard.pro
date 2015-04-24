-dontwarn javax.xml.stream.XMLStreamWriter
-dontwarn javax.xml.stream.XMLStreamException
-dontwarn com.caverock.androidsvg.R**

# XXX update twisterrob.pro in plugin
-keep public class * extends android.view.View {
	public <init>(android.content.Context);
	public <init>(android.content.Context, android.util.AttributeSet);
	public <init>(android.content.Context, android.util.AttributeSet, int);
}
# Probably worth listing all the support Views I use in xml
