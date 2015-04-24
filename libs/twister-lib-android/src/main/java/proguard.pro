# Warning: com.caverock.androidsvg.SVGImageView: can't find referenced class com.caverock.androidsvg.R
# Warning: com.caverock.androidsvg.SVGImageView: can't find referenced class com.caverock.androidsvg.R$styleable
-dontwarn com.caverock.androidsvg.SVGImageView
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# XXX Only for deubgging
-keepattributes SourceFile,LineNumberTable
