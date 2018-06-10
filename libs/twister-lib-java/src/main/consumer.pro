### -- twister-lib-java/proguard.pro -- ###

# Warning: net.twisterrob.java.io.IndentingXMLStreamWriter: can't find referenced class javax.xml.stream.XMLStreamWriter
-dontwarn javax.xml.stream.XMLStreamWriter
# Warning: net.twisterrob.java.io.IndentingXMLStreamWriter: can't find referenced class javax.xml.stream.XMLStreamException
-dontwarn javax.xml.stream.XMLStreamException

# Note: net.twisterrob.java.utils.CollectionTools accesses a declared field 'map|header|before|key|backingMap|prv' dynamically
#      Maybe this is library field 'class { type field; }'
# Ignore tryGetLastJava/tryGetLastAndroid
-dontnote net.twisterrob.java.utils.CollectionTools
