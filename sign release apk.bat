echo "Signing apk with jarsigner with key from mySkatemap.keystore"

"C:\Program Files\Android\Android Studio\jre\bin\jarsigner" -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore myskatemap.keystore "Y:\mySkatemap Track Recorder App\platforms\android\build\outputs\apk\android-release-unsigned.apk" "mySkatemap"

echo "Successfully signed apk"

echo "Optimizing signed apk with zipalign"

del "Y:\mySkatemap Track Recorder App\platforms\android\build\outputs\apk\mySkatemap.apk" /q /f

"C:\Users\Jan.hafner\AppData\Local\Android\sdk\build-tools\25.0.2\zipalign" -v 4 "Y:\mySkatemap Track Recorder App\platforms\android\build\outputs\apk\android-release-unsigned.apk" "Y:\mySkatemap Track Recorder App\platforms\android\build\outputs\apk\mySkatemap.apk"

echo "Successfully optimized apk"