#Introduction 


#Getting Started
1. npm install
2. ionic state reset
3. cordova plugin add cordova-plugin-googlemaps --save --variable API_KEY_FOR_ANDROID="%APIKEY%"
3. Change...
cordova.system.library.3=com.google.android.gms:play-services-location:9.8.0
cordova.system.library.4=com.android.support:support-v4:9.8.0
TO
cordova.system.library.3=com.google.android.gms:play-services-location:+
cordova.system.library.4=com.android.support:support-v4:+
AND/OR
cordova.system.library.1=com.google.android.gms:play-services-maps:+
cordova.system.library.2=com.google.android.gms:play-services-location:+