## Development Setup

 * For editing SQLite in `queries.xml` follow the **Tooling** guide in README.md of `:android:database`.

## Useful scripts

### Uninstall all packages from emulator

Careful if you have a device connected, it might uninstall production versions!

```batch
adb shell "pm list packages | grep --only-matching 'net.twisterrob.*' | while read package; do pm uninstall $package; done"
```

(Beware on Linux and Mac, the quotes might be different.)

### Run a specific test through AGP

```shell
gradlew :android:connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=net.twisterrob.inventory.android.activity.MainActivityTest_Drawer
```
