# Treasure
Treasure is an android `SharedPreferences` which support multi-process.

Treasure use `ContentProvider` to make sure read and write data in the same process, so it is safe for multi-process scenario.

The api of treasure is as same as `SharedPreferences`, so it is easy to migrate to treasure.

Treasure also supports:  
* memory mode
* key concerned `OnSharedPreferenceChangeListener`

## Getting start
Add following line to the `dependencies` section of your `build.gradle` file:

```gradle
compile 'org.xjy.android.treasure:treasure:1.0.2'
```

Then set the authority of `ContentProvider` used by treasure in the `android`-`defaultConfig` section of your `build.gradle` file like this: 

```gradle
android {

    defaultConfig {
        ...

        resValue "string", "org_xjy_android_treasure_authority", "your authority"
    }
}
```

To avoid conflict with other `ContentProvoder`, the best practice is named your authority with your package name, for instance: `org.xjy.android.treasure.sample`

Now you can use treasure in your code. First of all get a `TreasurePreferences` instant with a name, and then you can use the instant to read or write data.

Following code snipped illustrate the basic usage of `TreasurePrefereces` instant:

```java
TreasurePreferences treasurePreferences = TreasurePreferences.getInstance(this, "treasure");
int i = treasurePreferences.getInt("i", 0);
long l = treasurePreferences.getLong("l", 0);

treasurePreferences.edit().putString("s", "s").commit();
```

## Additional feature
**Memory mode** means data just be stored in memory, would not be persistented to disk.

If you want to use memory mode, just get `TreasurePreferences` instant with `TreasurePreferences.MODE_IN_MEMORY` param like following code snipped:

```java
TreasurePreferences.getInstance(this, "treasure", TreasurePreferences.MODE_IN_MEMORY);
```

We add an alternative key concerned `OnSharedPreferenceChangeListener` to improve performance. This listener just be invoked when the concerned keys changed.

You can register a key concerned `OnSharedPreferenceChangeListener` with concerned key list like this:

```java
mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "changed key:" + key);
    }
};
mTreasurePreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener, Arrays.asList("i", "s"));
```

**Note**: if you register key concerned listener in your `application` class without unregister it, make sure keep a listener reference in your `application` instance.
