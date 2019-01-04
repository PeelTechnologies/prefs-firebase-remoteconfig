# prefs-firebase-remoteconfig
Library to automatically sync SharedPrefs with Firebase RemoteConfig

# Usage
To automatically sync shared prefs with Firebase RemoteConfig, call:
```
TypedKey<String> key1 = new TypedKey<>("key1", String.class);
TypedKey<Boolean> key2 = new TypedKey<>("key2", Boolean.class);
PrefsFirebaseRemoteConfig remoteConfig = new PrefsFirebaseRemoteConfig(new Gson(), BuildConfig.DEBUG, key1, key1);
remoteConfig.sync();
```
The `sync()` method will fetch Firebase remote config, if the properties key1 and key2 are defined there, their values will be fetched. If these values are different from the locally stored values, `SharedPrefs` will be updated with the new values.

# Use with Gradle
add to your repositories

```
repositories {
    maven { url "https://jitpack.io" }
}
```

In your app build.gradle, add:  `compile "com.github.PeelTechnologies:prefs-firebase-remoteconfig:1.0.1"`
