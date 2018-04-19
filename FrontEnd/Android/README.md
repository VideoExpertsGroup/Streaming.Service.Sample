
# Linux


## requariements

* java-1.8


## Configure

Please configure local.properties

ndk.dir=/opt/android-ndk-r12b
sdk.dir=/opt/android-sdk-linux

## Build
	$ ./gradlew build

## Install to device 

Please use correct version

	$ adb install -r app/build/outputs/apk/app-debug.apk

