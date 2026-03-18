# react-native-glia-player

GliaPlayer React Native SDK, a WebView-based video ad player, into a React Native project.

## Scenarios
| Ads | Content Video|
|-------------------------|-------------------------|
|<img src="https://raw.githubusercontent.com/livingbio/GliaPlayer-Webview-Android-SDK/refs/heads/main/imgs/insert_ads.jpg" alt="insert_ads" width="200"/> |  <img src="https://raw.githubusercontent.com/livingbio/GliaPlayer-Webview-Android-SDK/refs/heads/main/imgs/insert_video.jpg" alt="insert_video" width="200"/> |
| <img src="https://raw.githubusercontent.com/livingbio/GliaPlayer-Webview-Android-SDK/refs/heads/main/imgs/floating_ads.jpg" alt="floating_ads" width="200"/>| <img src="https://raw.githubusercontent.com/livingbio/GliaPlayer-Webview-Android-SDK/refs/heads/main/imgs/floating_video.jpg" alt="floating_video" width="200"/>  |  


## Requirements

* React >=18.0.0
* React Native >=0.74.0
* Android minSdk 23+ 
* iOS platform 13.4+ 

## Installation


```sh
npm install @gliacloud/react-native-glia-player
```

## Android Configuration

1. In `gradle.properties`, add the following config:
```
newArchEnabled=true
```

2. Bypass `APPLICATION_ID` check for web view APIs for ads, in your app's `AndroidManifest.xml` file. To do so, add a <meta-data> tag with `android:name="com.google.android.gms.ads.INTEGRATION_MANAGER"`. For `android:value`, insert `webview`, surrounded by quotation marks.

```xml
<manifest>
  <application>
    <!-- Bypass APPLICATION_ID check for web view APIs for ads -->
    <meta-data
        android:name="com.google.android.gms.ads.INTEGRATION_MANAGER"
        android:value="webview"/>
  </application>
</manifest>
```

## iOS Configuration

In `Info.plist`, add this config:

```xml
<dict>
	<key>GADIntegrationManager</key>
	<string>webview</string>
  <key>RCTNewArchEnabled</key>
	<true/>
</dict>
```


## Usage


```js
import { GliaPlayerView } from "react-native-glia-player";

// ...

<GliaPlayerView
    slotKey="{your_slot_key}"
    style={styles.gliaplayer}
/>
```

## Sample Code

The sample code below shows how to add GliaPlayerView to the UI layout so that it always sticks to the bottom right corner of the screen.


```js
import React, { useMemo } from 'react';
import { View, ScrollView, StyleSheet } from 'react-native';
import { GliaPlayerView } from 'react-native-glia-player';

const getRandomColor = () => {
  const r = Math.floor(Math.random() * 256);
  const g = Math.floor(Math.random() * 256);
  const b = Math.floor(Math.random() * 256);
  return `rgb(${r}, ${g}, ${b})`;
};

export default function App() {
    const dummyBoxes = useMemo(() => {
    return Array.from({ length: 100 }).map((_, index) => (
      <View key={index} style={styles.boxContainer}>
        <View style={[styles.randomBox, { backgroundColor: getRandomColor() }]} />
      </View>
    ));
  }, []);

  return (
    <View style={styles.container}>
      <ScrollView style={styles.scrollView}>
        {dummyBoxes}
      </ScrollView>

      <View style={styles.playerContainer}>
        <GliaPlayerView
          slotKey="gliacloud_app_test"
          style={styles.gliaplayer}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1, // fillMaxSize
    backgroundColor: '#fff', 
  },
  scrollView: {
    flex: 1, // fillMaxSize
  },
  boxContainer: {
    marginBottom: 16, // Spacer(Modifier.height(16.dp))
  },
  randomBox: {
    width: '100%', // fillMaxWidth()
    height: 320,   // height(320.dp)
  },
  playerContainer: {
    position: 'absolute', // Allows it to float over the ScrollView
    bottom: 0,            // Alignment.Bottom...
    right: 0,             // ...End
    width: 320,           // width(480.dp)
    height: 240,          // height(320.dp)
    elevation: 5,         // Adds Android shadow (optional)
    zIndex: 5,            // Ensures it stays on top on iOS
  },
  gliaplayer: {
    flex: 1,
    width: '100%',
    height: '100%',
  },
});
```

## License

MIT
