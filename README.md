Useful commands
---------------

Build Everything

```
gradlew clean assemble assembleDebugAndroidTest compileDebugUnitTestSources -x :android:lintVitalRelease --no-build-cache
```

Run all UI tests
```
gradlew connectedCheck --continue mergeAndroidReports
```


Useful Settings
---------------

From `Magic Home Inventory.iws`:
```xml
<component name="AndroidConfiguredLogFilters">
  <filters>
    <filter>
      <option name="logLevel" value="verbose" />
      <option name="logMessagePattern" value="^(?!Unable to resolve superclass of|Link of class|DexOpt: unable to opt direct call|Could not find class|Could not find method|VFY: |.*\.onUserInteraction\(\)|DecodeImagePath\(decodeResourceStream\d+\)|performCreate Call )" />
      <option name="logTagPattern" value="^(?!AbsListView|endeffect|GestureDetector|CustomFrequencyManager|ApplicationPackageManager|PersonaManager|ProgressBar|ViewRootImpl|MotionRecognitionManager|Timeline|ArrayMap)" />
      <option name="name" value="app: net.twisterrob.inventory.debug" />
      <option name="packageNamePattern" value="net.twisterrob.inventory.debug" />
      <option name="pid" value="" />
    </filter>
    <filter>
      <option name="logLevel" value="verbose" />
      <option name="logMessagePattern" value="^(?!TIMA: QCOM_send_command|.*TIMA_PKM_measure_kernel|rsp_len = |getCSCPackageItemText\(\)|DCD OFF)" />
      <option name="logTagPattern" value="^(?!EDMNativeHelper|EnterpriseDeviceManager|ServiceKeeper|SSRMv2:(Monitor|AmoledAdjustTimer)|STATUSBAR-(IconMerger|PhoneStatusBar|NetworkController)|PersonaManager|KeyguardUpdateMonitor|BatteryService|BatteryMeterView|AwesomePlayer|AudioPlayer|AudioCache|OMX.*|AudioPolicyManagerBase|MediaPlayerService|StagefrightPlayer|OggExtractor|SecMediaClock|MP-Decision|ThermalEngine|MSim-SignalClusterView|StatusBar.MSimNetworkController|ConnectivityService|WifiStateMachine|Prime31|installd|SecCameraCoreManager|mm-camera-sensor)" />
      <option name="name" value="Global with Less Noise" />
      <option name="packageNamePattern" value="" />
      <option name="pid" value="" />
    </filter>
    <filter>
      <option name="logLevel" value="verbose" />
      <option name="logMessagePattern" value="" />
      <option name="logTagPattern" value="^(?!AbsListView|IInputConnectionWrapper|ApplicationPackageManager)" />
      <option name="name" value="app: net.twisterrob.inventory" />
      <option name="packageNamePattern" value="net.twisterrob.inventory" />
      <option name="pid" value="" />
    </filter>
  </filters>
</component>
<component name="AndroidGradleBuildConfiguration">
  <option name="USE_CONFIGURATION_ON_DEMAND" value="false" />
  <option name="COMMAND_LINE_OPTIONS" value="-D--refresh-dependencies -D--offline --stacktrace -D--info" />
</component>
```

External file sources
---------------------

`ic_launcher` idea from http://www.rgbstock.com/bigphoto/meMHCk0/Smiley+Box

`ic_action_select_all` and `ic_action_select_invert` icons from:
 * http://google.github.io/material-design-icons/ (sources: https://github.com/google/material-design-icons)

More can be found at http://materialdesignicons.com/.
