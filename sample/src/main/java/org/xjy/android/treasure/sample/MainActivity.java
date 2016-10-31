package org.xjy.android.treasure.sample;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.xjy.android.treasure.TreasurePreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TreasureSample";

    private TreasurePreferences mTreasurePreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        mTreasurePreferences = TreasurePreferences.getInstance(this, "treasure");
//        mTreasurePreferences = TreasurePreferences.getInstance(this, "treasure", TreasurePreferences.MODE_IN_MEMORY);
        HashSet<String> ss = new HashSet<>();
        ss.add("s1");
        ss.add("s2");
        mTreasurePreferences.edit().putBoolean("b", true).putFloat("f", 1.0f).putInt("i", 1).putLong("l", 1).putString("s", "s").putStringSet("ss", ss).commit();

        mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d(TAG, "changed key:" + key);
                Log.d(TAG, sharedPreferences.getAll().get(key).toString());
            }
        };
        mTreasurePreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener, new ArrayList<String>(Arrays.asList("b", "f", "i", "l", "s", "ss")));
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();

        Map<String, ?> map = mTreasurePreferences.getAll();
        Log.d(TAG, "value:" + map);
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Log.d(TAG, "key:" + entry.getKey() + " value:" + entry.getValue() + " value_type:" + entry.getValue().getClass());
        }
        Log.d(TAG, "b:" + mTreasurePreferences.getBoolean("b", false));
        Log.d(TAG, "f:" + mTreasurePreferences.getFloat("f", 0));
        Log.d(TAG, "i:" + mTreasurePreferences.getInt("i", 0));
        Log.d(TAG, "l:" + mTreasurePreferences.getLong("l", 0));
        Log.d(TAG, "s:" + mTreasurePreferences.getString("s", null));
        Log.d(TAG, "ss:" + mTreasurePreferences.getStringSet("ss", null));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTreasurePreferences.edit().putString("s", "freedom").apply();
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTreasurePreferences.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }
}
