package org.xjy.android.treasure;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xjy.android.treasure.provider.TreasureContract;
import org.xjy.android.treasure.provider.TreasureProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class TreasurePreferences implements SharedPreferences {
    public static final int MODE_DEFAULT = 0;
    public static final int MODE_IN_MEMORY = 1;

    public static final int TYPE_STRING = 1;
    public static final int TYPE_STRING_SET = 2;
    public static final int TYPE_INT = 3;
    public static final int TYPE_LONG = 4;
    public static final int TYPE_FLOAT = 5;
    public static final int TYPE_BOOLEAN = 6;

    private Context mContext;
    private String mName;
    private int mMode;
    private final WeakHashMap<OnSharedPreferenceChangeListener, List<String>> mListeners = new WeakHashMap<>();
    private BroadcastReceiver mPreferencesChangeReceiver;
    private static final HashMap<String, TreasurePreferences> sPrefers = new HashMap<>();

    private TreasurePreferences(Context context, String name, int mode) {
        mContext = context.getApplicationContext();
        mName = name;
        mMode = mode;
    }

    public static TreasurePreferences getInstance(Context context, String name) {
        return getInstance(context, name, MODE_DEFAULT);
    }

    public static TreasurePreferences getInstance(Context context, String name, int mode) {
        synchronized (sPrefers) {
            TreasurePreferences tp = sPrefers.get(name);
            if (tp == null) {
                tp = new TreasurePreferences(context, name, mode);
                sPrefers.put(name, tp);
            }
            return tp;
        }
    }

    @Override
    public Map<String, ?> getAll() {
        HashMap<String, Object> map = new HashMap<>();
        ContentProviderClient client = null;
        Cursor cursor = null;
        try {
            Uri uri = buildUri(TreasureContract.QUERY_GET_ALL, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            cursor = localProvider != null ? localProvider.query(uri, null, null, null, null) : contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
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
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
            releaseClientSilently(client);
        }
        return map;
    }

    @Override
    public String getString(String key, String defValue) {
        ContentProviderClient client = null;
        Cursor cursor = null;
        try {
            Uri uri = buildUri(TreasureContract.QUERY_GET, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            cursor = localProvider != null ? localProvider.query(uri, new String[]{key}, null, null, TYPE_STRING + "") : contentResolver.query(uri, new String[]{key}, null, null, TYPE_STRING + "");
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getString(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
            releaseClientSilently(client);
        }
        return defValue;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        ContentProviderClient client = null;
        Cursor cursor = null;
        try {
            Uri uri = buildUri(TreasureContract.QUERY_GET, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            cursor = localProvider != null ? localProvider.query(uri, new String[]{key}, null, null, TYPE_STRING_SET + "") : contentResolver.query(uri, new String[]{key}, null, null, TYPE_STRING_SET + "");
            if (cursor != null && cursor.moveToNext()) {
                return TreasureProvider.jsonArrayToStringSet(new JSONArray(cursor.getString(0)));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
            releaseClientSilently(client);
        }
        return defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        ContentProviderClient client = null;
        Cursor cursor = null;
        try {
            Uri uri = buildUri(TreasureContract.QUERY_GET, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            cursor = localProvider != null ? localProvider.query(uri, new String[]{key}, defValue + "", null, TYPE_INT + "") : contentResolver.query(uri, new String[]{key}, defValue + "", null, TYPE_INT + "");
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
            releaseClientSilently(client);
        }
        return defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        ContentProviderClient client = null;
        Cursor cursor = null;
        try {
            Uri uri = buildUri(TreasureContract.QUERY_GET, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            cursor = localProvider != null ? localProvider.query(uri, new String[]{key}, defValue + "", null, TYPE_LONG + "") : contentResolver.query(uri, new String[]{key}, defValue + "", null, TYPE_LONG + "");
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getLong(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
            releaseClientSilently(client);
        }
        return defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        ContentProviderClient client = null;
        Cursor cursor = null;
        try {
            Uri uri = buildUri(TreasureContract.QUERY_GET, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            cursor = localProvider != null ? localProvider.query(uri, new String[]{key}, defValue + "", null, TYPE_FLOAT + "") : contentResolver.query(uri, new String[]{key}, defValue + "", null, TYPE_FLOAT + "");
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getFloat(0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
            releaseClientSilently(client);
        }
        return defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        ContentProviderClient client = null;
        Cursor cursor = null;
        try {
            Uri uri = buildUri(TreasureContract.QUERY_GET, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            cursor = localProvider != null ? localProvider.query(uri, new String[]{key}, defValue + "", null, TYPE_BOOLEAN + "") : contentResolver.query(uri, new String[]{key}, defValue + "", null, TYPE_BOOLEAN + "");
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(0) == 1;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
            releaseClientSilently(client);
        }
        return defValue;
    }

    @Override
    public boolean contains(String key) {
        ContentProviderClient client = null;
        Cursor cursor = null;
        try {
            Uri uri = buildUri(TreasureContract.QUERY_CONTAINS, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            cursor = localProvider != null ? localProvider.query(uri, new String[]{key}, null, null, null) : contentResolver.query(uri, new String[]{key}, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(0) == 1;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            closeCursorSilently(cursor);
            releaseClientSilently(client);
        }
        return false;
    }

    @Override
    public Editor edit() {
        return new TreasureEditor();
    }

    /**
     * This method is expensive.
     * You should use {@link #registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener, List keys)} instead to achieve performance.
     * */
    @Deprecated
    @Override
    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener listener) {
        if (listener == null) {
            return;
        }
        synchronized(mListeners) {
            mListeners.put(listener, null);
            registerChangeReceiver();
        }
        ContentProviderClient client = null;
        try {
            Uri uri = buildUri(TreasureContract.REGISTER, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            ContentValues values = new ContentValues();
            values.put(TreasureProvider.KEYS, (String) null);
            if (localProvider != null) {
                localProvider.insert(uri, values);
            } else {
                contentResolver.insert(uri, values);
            }
        } finally {
            releaseClientSilently(client);
        }
    }

    /**
     * @param listener The callback that will run.
     * @param keys The keys that be listened.
     * */
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener, List<String> keys) {
        if (listener == null || keys == null || keys.size() == 0) {
            return;
        }
        synchronized (mListeners) {
            mListeners.put(listener, keys);
            registerChangeReceiver();
        }
        ContentProviderClient client = null;
        try {
            Uri uri = buildUri(TreasureContract.REGISTER, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            ContentValues values = new ContentValues();
            values.put(TreasureProvider.KEYS, new JSONArray(keys).toString());
            if (localProvider != null) {
                localProvider.insert(uri, values);
            } else {
                contentResolver.insert(uri, values);
            }
        } finally {
            releaseClientSilently(client);
        }
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        if (listener == null) {
            return;
        }
        String[] keys = null;
        synchronized(mListeners) {
            List<String> keyList = mListeners.remove(listener);
            if (keyList != null) {
                keys = keyList.toArray(new String[keyList.size()]);
            }
            if (mListeners.size() == 0) {
                mContext.unregisterReceiver(mPreferencesChangeReceiver);
            }
        }
        ContentProviderClient client = null;
        try {
            Uri uri = buildUri(TreasureContract.UNREGISTER, null);
            ContentResolver contentResolver = mContext.getContentResolver();
            client = contentResolver.acquireContentProviderClient(uri);
            ContentProvider localProvider = client.getLocalContentProvider();
            if (localProvider != null) {
                localProvider.delete(uri, null, keys);
            } else {
                contentResolver.delete(uri, null, keys);
            }
        } finally {
            releaseClientSilently(client);
        }
    }

    private void registerChangeReceiver() {
        if (mPreferencesChangeReceiver == null) {
            mPreferencesChangeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String name = intent.getStringExtra(TreasureProvider.EXTRA_NAME);
                    if (mName.equals(name)) {
                        ArrayList<String> modifiedKeys = (ArrayList<String>) intent.getSerializableExtra(TreasureProvider.EXTRA_KEYS);
                        ArrayList<Pair<OnSharedPreferenceChangeListener, String>> listeners = new ArrayList<>();
                        synchronized (mListeners) {
                            for (Map.Entry<OnSharedPreferenceChangeListener, List<String>> entry : mListeners.entrySet()) {
                                List<String> keys = entry.getValue();
                                for (int i = modifiedKeys.size() - 1; i >= 0; i--) {
                                    String key = modifiedKeys.get(i);
                                    if (keys == null || keys.contains(key)) {
                                        listeners.add(new Pair<>(entry.getKey(), key));
                                    }
                                }
                            }
                        }
                        for (int i = listeners.size() - 1; i >= 0; i--) {
                            Pair<OnSharedPreferenceChangeListener, String> pair = listeners.get(i);
                            pair.first.onSharedPreferenceChanged(TreasurePreferences.this, pair.second);
                        }
                    }
                }
            };
            mContext.registerReceiver(mPreferencesChangeReceiver, new IntentFilter(TreasureProvider.ACTION_PREFERENCES_CHANGE));
        }
    }

    private Uri buildUri(String path, HashMap<String, String> params) {
        Uri.Builder builder = TreasureContract.getAuthorityUri(mContext).buildUpon();
        builder.appendPath(mName).appendPath(mMode + "").appendPath(path);
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
            } catch (Throwable ignored) {}
        }
    }

    private void releaseClientSilently(ContentProviderClient client) {
        if (client != null) {
            try {
                client.release();
            } catch (Throwable ignored) {}
        }
    }

    public final class TreasureEditor implements Editor {
        private final Map<String, Object> mModified = new HashMap<>();
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
                mModified.put(key, (values == null) ? null : new HashSet<>(values));
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
                ArrayList<String> stringSetKeyList = new ArrayList<>();
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
                ContentProviderClient client = null;
                try {
                    HashMap<String, String> params = new HashMap<>();
                    params.put(TreasureContract.PARAM_CLEAR, mClear + "");
                    params.put(TreasureContract.PARAM_IMMEDIATELY, immediately + "");
                    Uri uri = buildUri(TreasureContract.UPDATE, params);
                    ContentResolver contentResolver = mContext.getContentResolver();
                    client = contentResolver.acquireContentProviderClient(uri);
                    ContentProvider localProvider = client.getLocalContentProvider();
                    if (localProvider != null) {
                        localProvider.update(uri, contentValues, stringSetValueArray.toString(), stringSetKeyList.size() > 0 ? stringSetKeyList.toArray(new String[stringSetKeyList.size()]) : null);
                    } else {
                        contentResolver.update(uri, contentValues, stringSetValueArray.toString(), stringSetKeyList.size() > 0 ? stringSetKeyList.toArray(new String[stringSetKeyList.size()]) : null);
                    }
                } finally {
                    releaseClientSilently(client);
                }
            }
        }
    }
}
