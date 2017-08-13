#Getting Started
1. npm install
2. ionic state reset
3. cordova plugin add https://github.com/mapsplugin/cordova-plugin-googlemaps-sdk --nofetch
4. cordova plugin add https://github.com/mapsplugin/cordova-plugin-googlemaps#multiple_maps --save --variable API_KEY_FOR_ANDROID="%APIKEY%"
5. Change...
cordova.system.library.3=com.google.android.gms:play-services-location:9.8.0
cordova.system.library.4=com.android.support:support-v4:9.8.0
TO
cordova.system.library.3=com.google.android.gms:play-services-location:+
cordova.system.library.4=com.android.support:support-v4:+
AND/OR
cordova.system.library.1=com.google.android.gms:play-services-maps:+
cordova.system.library.2=com.google.android.gms:play-services-location:+

#Build
Run the following command to build the app using the Ionic CLI as **unoptimized debug** build:
```
> ionic cordova build android
```

Run the following command to build the app using the Ionic CLI as **optimized release** build:
```
> ionic cordova build android --release --device --production
```

#Sign release package
Make sure paths are properly set!
Run the following command to sign the release package:
```
> sign release apk.bat
```

Run the following command to push the release package, via usb, to the attached android device:
```
> push signed release apk to android.bat
```

Run the following command to sign/push the release package , via usb, to the attached android device:
```
> sign release and push apk to android.bat
```