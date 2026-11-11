# Fire TV Suite

Android TV apps for Fire TV Home and Live TV, Cinema, and Fire Volume. The
repository contains source and build instructions. It does not include
accounts, playlists, subscriptions, guide dumps, bundled artwork, test
media, or release signing keys.

## Build

On Windows, use PowerShell 7, Python 3.9+, a JDK with Java 8 cross-compilation
support, Android SDK platform 35 or later, Android build tools (aapt2,
zipalign, and apksigner), an R8 JAR, and the upstream VideoLAN
`org.videolan.android:libvlc-all:3.7.7` AAR. Supply the tools and AAR locally.

```powershell
./build.ps1 -AndroidJar path/to/android.jar -Aapt2 path/to/aapt2.exe `
  -R8Jar path/to/r8.jar -LibVlcAar path/to/libvlc-all-3.7.7.aar
```

The output is three unsigned APKs in `build/`. Align and sign each APK
locally before installing. For a fresh installation, use your own signing
key and keep it for future updates. Fire Volume's system-wide remote button
handling needs a device-local ADB grant of READ_LOGS and overlay access.
Game streaming opens an independently installed Open Game Stream app.

Cinema's add-on URL and Live TV's provider or playlist are entered through
the apps. The optional EPG seed, local artwork, and trivia pack from the
private build are omitted. The apps can fetch their normal runtime data.
A Velvet PIN is chosen on the device at first use of that feature. Weather
location is set on the device.

## Install an update and keep data

Use the same application ID and original signing certificate for each app.
Increase `android:versionCode` for each release. Keep the signing key and its
password outside Git. Existing provider credentials are encrypted with a
device-local Android Keystore key. The update retains the existing preference
files and Keystore aliases. A Git push alone does not install a release.

| App | Application ID |
| --- | --- |
| Fire TV Home and Live TV | `local.pitv` |
| Cinema | `local.ondemand` |
| Fire Volume | `local.firevolume` |

After aligning and signing with the original key, install over the existing app:

```powershell
adb -s DEVICE_SERIAL install -r path/to/signed-update.apk
```

If Android rejects a signature or version mismatch, correct the APK and retry.
Uninstalling or clearing app data deletes local settings and credentials.
Before distributing a release, test an in-place upgrade using disposable
settings and a test login. Never bundle a provisioned APK.

## License

The original project code is MIT licensed. The VideoLAN AWindow patch is
LGPL-2.1-or-later and retains its upstream copyright and license notice.
Upstream LibVLC is obtained separately. If you distribute built APKs, fulfill
the license terms of the libraries you include.
