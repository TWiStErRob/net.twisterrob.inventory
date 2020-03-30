## Setup
```cmd
p:\tools\sdk\google-cloud-sdk>
install
rem Do you want to help improve the Google Cloud SDK (y/N)?  y
rem Update %PATH% to include Cloud SDK binaries? (Y/n)?  n

cd bin
gcloud components update
gcloud auth login
rem select papp.robert.s@gmail.com
gcloud config set project twisterrob-inventory
```

## Dump configuration parameters
```cmd
gcloud firebase test android models list > android.txt
gcloud firebase test android versions list > versions.txt
gcloud firebase test android locales list > locales.txt
gcloud firebase test android models describe Nexus6 > device_Nexus6.txt
```

## Execute tests
See https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run.

Currently Testlab was only used to check AndroidSVG library's upgrade compatibility, see [../android/data/testlab/scripts/].
