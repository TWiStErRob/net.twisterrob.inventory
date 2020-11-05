Without configuration using the SLF4J Logger in an Android app will result in auto-truncated log tags like this: `a*.b*.c**EndOfClassName`.
This is because Android only supports tags up to 23 characters long and the default logic is to shorten as much as possible while trying to keep uniqueness.

To configure better logs create an `src/main/resources/android-logger.properties` file and add some substitution rules. 

Recommended default package ignores for the libraries.
```properties
replacement.android=net\\.twisterrob\\.android\\.(.+\\.)?
replacement.java=^net\\.twisterrob\\.java\\.(.+\\.)?
```

For an app using a relative sub-package name and class name:
```properties
replacement.inventory=^net\\.twisterrob\\.someapp\\.(.+\\.)?
```

It is possible to do replacements to clarify things:
```properties
replacement.domain=^net\\.twisterrob\\.
replacement.domain.with=tws::
replacement.myapp=^tws::someapp\\.
replacement.myapp.with=myapp.
```
_Multiple replacements will be applied, but it may depend on hash order. So don't do the above, as it may or may not result in these:_
```
net.twisterrob.foo.Bar -> tws::foo.Bar
net.twisterrob.someapp.foo.Bar -> myapp.foo.Bar
```

It is possible to do replacements with proper substitutions to clarify things even further:
```properties
replacement.inventory=^net\\.twisterrob\\.someapp\\.(.+\\.)?
replacement.inventory.with=myapp.$1
```
Use `$1`, `$2`, etc. for captured groups in the first regex.
