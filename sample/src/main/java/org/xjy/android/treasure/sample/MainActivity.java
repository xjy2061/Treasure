package org.xjy.android.treasure.sample;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import org.xjy.android.treasure.TreasurePreferences;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private TreasurePreferences mTreasurePreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        mTreasurePreferences = new TreasurePreferences(this, "treasure");
        HashSet<String> ss = new HashSet<>();
        ss.add("ss");
        ss.add("sss");
        mTreasurePreferences.edit().putBoolean("b", true).putFloat("f", 1.0f).putInt("i", 1).putLong("l", 1).putString("s", "s").putStringSet("ss", ss).commit();

        mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                System.out.println(">>>change key: " + key);
                System.out.println(">>>value:" + sharedPreferences.getAll());
            }
        };
        mTreasurePreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTreasurePreferences.edit().putString("s", "freedom").apply();
            }
        }, 5000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTreasurePreferences.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }
}
