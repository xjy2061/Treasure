package org.xjy.android.treasure;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xjy.android.treasure.provider.TreasureContract;
import org.xjy.android.treasure.provider.TreasureProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class TreasurePreferences implements SharedPreferences {
    public static final int TYPE_STRING = 1;
    public static final int TYPE_STRING_SET = 2;
    public static final int TYPE_INT = 3;
    public static final int TYPE_LONG = 4;
    public static final int TYPE_FLOAT = 5;
    public static final int TYPE_BOOLEAN = 6;

    private Context mContext;
    private String mName;
    private static final Object mContent = new Object();
    private final WeakHashMap<OnSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap<OnSharedPreferenceChangeListener, Object>();
    private BroadcastReceiver mPreferencesChangeReceiver;
    private static HashMap<String, TreasurePreferences> sPrefers = new HashMap<String, TreasurePreferences>();

    private TreasurePreferences(Context context, String name) {
        mContext = context.getApplicationContext();
        mName = name;
    }

    public static TreasurePreferences getInstance(Context context, String name) {
        synchronized (sPrefers) {
            TreasurePreferences tp = sPrefers.get(name);
            if (tp == null) {
                tp = new TreasurePreferences(context, name);
                sPrefers.put(name, tp);
            }
            return tp;
        }
    }

    @Override
    public Map<String, ?> getAll() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET_ALL, null), null, null, null, null);
            while (cursor.moveToNext()) {
                JSONObject json = new JSONObject(cursor.getString(0));
                for (Iterator<String> it = json.keys(); it.hasNext();) {
                    String key = it.next();
                    if (json.isNull(key)) {
                        map.put(key, null);
                    } else {
                        JSONArray array = json.getJSONArray(key);
                        int type = array.getInt(0);
                        switch (type) {
                            case TreasurePreferences.TYPE_STRING:
                                map.put(key, array.getString(1));
                                break;
                            case TreasurePreferences.TYPE_STRING_SET:
                                map.put(key, TreasureProvider.jsonArrayToStringSet(array.getJSONArray(1)));
                                break;
                            case TreasurePreferences.TYPE_INT:
                                map.put(key, array.getInt(1));
                                break;
                            case TreasurePreferences.TYPE_LONG:
                                map.put(key, array.getLong(1));
                                break;
                            case TreasurePreferences.TYPE_FLOAT:
                                float f = (float) array.getDouble(1);
                                map.put(key, f);
                                break;
                            case TreasurePreferences.TYPE_BOOLEAN:
                                map.put(key, array.getBoolean(1));
                                break;
                        }
                    }
                }

            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return map;
    }

    @Override
    public String getString(String key, String defValue) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, null, null, TYPE_STRING + "");
            while (cursor.moveToNext()) {
                return cursor.getString(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValue;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, null, null, TYPE_STRING_SET + "");
            while (cursor.moveToNext()) {
                return TreasureProvider.jsonArrayToStringSet(new JSONArray(cursor.getString(0)));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, defValue + "", null, TYPE_INT + "");
            while (cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, defValue + "", null, TYPE_LONG + "");
            while (cursor.moveToNext()) {
                return cursor.getLong(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, defValue + "", null, TYPE_FLOAT + "");
            while (cursor.moveToNext()) {
                return cursor.getFloat(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_GET, null), new String[]{key}, defValue + "", null, TYPE_BOOLEAN + "");
            while (cursor.moveToNext()) {
                return cursor.getInt(0) == 1;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return defValue;
    }

    @Override
    public boolean contains(String key) {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(buildUri(TreasureContract.QUERY_CONTAINS, null), new String[]{key}, null, null, null);
            while (cursor.moveToNext()) {
                return cursor.getInt(0) == 1;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
        }
        return false;
    }

    @Override
    public Editor edit() {
        return new TreasureEditor();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        if (listener == null) {
            return;
        }
        synchronized(this) {
            mListeners.put(listener, mContent);
            if (mListeners.size() == 1) {
                mPreferencesChangeReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String key = intent.getStringExtra(TreasureProvider.EXTRA_KEY);
                        HashSet<OnSharedPreferenceChangeListener> listeners;
                        synchronized (TreasurePreferences.this) {
                            listeners = new HashSet<OnSharedPreferenceChangeListener>(mListeners.keySet());
                        }
                        for (OnSharedPreferenceChangeListener l : listeners) {
                            l.onSharedPreferenceChanged(TreasurePreferences.this, key);
                        }
                    }
                };
                mContext.registerReceiver(mPreferencesChangeReceiver, new IntentFilter(TreasureProvider.ACTION_PREFERENCES_CHANGE));
                mContext.getContentResolver().insert(buildUri(TreasureContract.REGISTER, null), null);
            }
        }
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        if (listener == null) {
            return;
        }
        synchronized(this) {
            mListeners.remove(listener);
            if (mListeners.size() == 0) {
                mContext.unregisterReceiver(mPreferencesChangeReceiver);
                mContext.getContentResolver().delete(buildUri(TreasureContract.UNREGISTER, null), null, null);
            }
        }
    }

    private Uri buildUri(String path, HashMap<String, String> params) {
        Uri.Builder builder = TreasureContract.getAuthorityUri(mContext).buildUpon();
        builder.appendPath(mName).appendPath(path);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    private void closeCursorSilently(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable t) {}
        }
    }

    public final class TreasureEditor implements Editor {
        private final Map<String, Object> mModified = new HashMap<String, Object>();
        private boolean mClear = false;

        @Override
        public Editor putString(String key, String value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            synchronized (this) {
                mModified.put(key, (values == null) ? null : new HashSet<String>(values));
                return this;
            }
        }

        @Override
        public Editor putInt(String key, int value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putLong(String key, long value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putFloat(String key, float value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor remove(String key) {
            synchronized (this) {
                mModified.put(key, null);
                return this;
            }
        }

        @Override
        public Editor clear() {
            synchronized (this) {
                mClear = true;
                return this;
            }
        }

        @Override
        public boolean commit() {
            update(true);
            return true;
        }

        @Override
        public void apply() {
            update(false);
        }

        private void update(boolean immediately) {
            synchronized (this) {
                ContentValues contentValues = new ContentValues();
                ArrayList<String> stringSetKeyList = new ArrayList<String>();
                JSONArray stringSetValueArray = new JSONArray();
                for (Map.Entry<String, Object> entry : mModified.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value == null) {
                        contentValues.putNull(key);
                    } else if (value instanceof String) {
                        contentValues.put(key, (String) value);
                    } else if (value instanceof HashSet) {
                        stringSetKeyList.add(key);
                        stringSetValueArray.put(TreasureProvider.stringSetToJSONArray((HashSet<String>) value));
                    } else if (value instanceof Integer) {
                        contentValues.put(key, (Integer) value);
                    } else if (value instanceof Long) {
                        contentValues.put(key, (Long) value);
                    } else if (value instanceof Float) {
                        contentValues.put(key, (Float) value);
                    } else if (value instanceof Boolean) {
                        contentValues.put(key, (Boolean) value);
                    }
                }
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(TreasureContract.PARAM_CLEAR, mClear + "");
                params.put(TreasureContract.PARAM_IMMEDIATELY, immediately + "");
                mContext.getContentResolver().update(buildUri(TreasureContract.UPDATE, params), contentValues, stringSetValueArray.toString(), stringSetKeyList.size() > 0 ? stringSetKeyList.toArray(new String[stringSetKeyList.size()]) : null);
            }
        }
    }
}
