## The magic of `queries.xml`
The goal: straight SQL query with compile-time reference check and it should be copy-pastable, well-formatted and correctly highlighted.
```xml
<string name="query_foo" translatable="false">"<![CDATA[
    select
        tab.column             as alias,
        'constant string'      as data
        FUNCTION(param)        as calculated
   from Table tab
   where tab._id <> 0
]]>"</string>
```
### Breaking it down
 * `<string>` &rarr; `R.string.query_foo` is a compile-time reference
 * `query_` prefix for easy identification
 * `foo` is the name of the query
 * `translatable="false"` because we don't need different queries based on locales
 * `<![CDATA[...]]>` allows for using [`<>&?@`](https://developer.android.com/guide/topics/resources/string-resource.html#escaping_quotes) literally without escaping; `"` inside still needs `\"`
 * Wrapping `string` inside `"..."` allows for using `'` literally without backslash in front of it. This is not [documented](https://developer.android.com/guide/topics/resources/string-resource.html#String), sadly.  
   _Since around AGP 3.3 (probably AAPT2) `"` needs to be outside CDATA, previously `<![CDATA["` also worked._

### Tooling
SQLScout gives a nice SQLite highlighter. It highlights occurrences of tables. The highlighter is injectable as a language. It has some bugs not recognising `FROM (<subquery with union>)`, `[<escaped column alias>]` and `EXISTS` keywords, but it's mostly useful.

Setup:
 1. Install [SQLScout](https://plugins.jetbrains.com/plugin/8322-sqlscout-sqlite-support-) and [XPathView + XSLT](https://plugins.jetbrains.com/plugin/12478-xpathview--xslt)*  
    _In IntelliJ Ultimate there's a plugin called **Database Tools and SQL** which provides **SQLite (SQL)** injectable language out of the box._

 1. Go to `res/values/queries.xml`
 1. Go inside a query and invoke **Show Intention Actions** (<kbd>Alt+Enter</kbd>)
 1. Select **Inject language or reference**
 1. Select **SQLite (SQL SQLite Dialect)**
 1. _(at this point all `<string>`s will be highlighted, even text)_
 1. Go inside a query and invoke **Show Intention Actions** (<kbd>Alt+Enter</kbd>)
 1. Select **Language Injection Settings**
 1. Enter **Advanced > Value pattern**: `^"(.*)"$`  
    _This will select only the query for editing the language fragment._
 1. Enter **Advanced > XPath condition**: `@translatable='false' and starts-with(@name, 'query_')`  
    _This will make sure that the normal textual strings are not syntax highlighted, only the queries._

For more info see [documentation](https://www.jetbrains.com/help/idea/language-injection-settings-dialog-xml-tag-injection.html).  

\* _The **XPath condition** requires the XPathView plugin. [XPathView + XSLT](https://plugins.jetbrains.com/plugin/12478-xpathview--xslt) is the one, [XPathView + XSLT Support](https://plugins.jetbrains.com/plugin/237-xpathview--xslt-support) was abandoned in 2008. If Android Studio version is not compatible, even the new plugin may need some [hacking](https://plugins.jetbrains.com/plugin/12478-xpathview--xslt/reviews#review=37569)._

### Alternative
This also works similarly:
```xml
<string name="query_foo" translatable="false"><![CDATA[--"
...
--"]]></string>
```
The drawback here is that the actual runtime string will also contain these characters.
 * `--` is required since around AGP 3.3 (probably AAPT2) to escape `"`. Since the queries are multi-line anyway starting and ending with a comment is no problem.
 * `--` is placed inside CDATA so that CDATA is not highlighted as a comment  
   `"` also needs to be placed inside for the same reason

For tooling, the **Advanced > Value pattern** for this is `^--"(.*)--"$`, the rest is the same.
