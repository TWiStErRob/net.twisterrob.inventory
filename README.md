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

Release play-by-play
--------------------

1. Fix all warnings `gradlew clean releaseCheck` should yield 0 warnings.
1. Commit all changes (`svn status` should be empty)
1. `svn update` just to be sure (double-check with `svn info`)
1. `gradlew clean releaseRelease` (1m before ProGuard, 1m for ProGuard)
1. Upload `p:\repos\release\android\net.twisterrob.inventory@*.zip`:
    * `.apk` @ Developer Console > APK > [Alpha](https://play.google.com/apps/publish/?dev_acc=01909946919088079965#ApkPlace:p=net.twisterrob.inventory)
    * `mapping.txt` @ [Deobfuscation](https://play.google.com/apps/publish/?dev_acc=01909946919088079965#DeobfuscationMappingFilesPlace:p=net.twisterrob.inventory)
1. Update http://www.twisterrob.net/project/inventory/ with release notes
1. Update *What's new in this version?* based on previous one
1. Make a backup of current version on the phone with
    ```bash
    adb backup -f Inventory-pre<version>.ab -apk -noobb -noshared -nosystem net.twisterrob.inventory
    ```
1. Make sure the installed release version has all kinds of data
1. Publish to Alpha
1. Check [Pre-launch Report](https://play.google.com/apps/publish/?dev_acc=01909946919088079965#PreLaunchReportPlace:p=net.twisterrob.inventory)
1. Generated in about 15min after upload, if errors, then start again
1. Wait until alpha stage is propagated and update current release version on my phone
1. Smoke test for no errors
1. If no errors Promote to Beta or Prod
1. Update version number in android/build.gradle anticipating minor and commit
1. Send out emails to people who requested features.

External file sources
---------------------

`ic_launcher` idea from http://www.rgbstock.com/bigphoto/meMHCk0/Smiley+Box

`ic_action_select_all` and `ic_action_select_invert` icons from:
 * http://google.github.io/material-design-icons/ (sources: https://github.com/google/material-design-icons)

More can be found at http://materialdesignicons.com/.
