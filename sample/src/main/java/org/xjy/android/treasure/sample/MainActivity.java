package org.xjy.android.treasure.sample;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xjy.android.treasure.R;
import org.xjy.android.treasure.TreasurePreferences;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        TreasurePreferences tp = new TreasurePreferences(this, "kk");
        HashSet<String> ss = new HashSet<>();
        ss.add("ss");
        ss.add("sss");
        tp.edit().putBoolean("b", true).putFloat("f", 1.0f).putInt("i", 1).putLong("l", 1).putString("s", "s").putStringSet("ss", ss).commit();

    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        TreasurePreferences tp = new TreasurePreferences(this, "kk");
//        tp.edit().putStringSet("ss", null).apply();
        System.out.println(">>>" + tp.getStringSet("ss", null));
    }
}
