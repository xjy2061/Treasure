package org.xjy.android.treasure.provider;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xjy.android.treasure.TreasurePreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TreasureProvider extends ContentProvider {
    private static final int QUERY_GET = 1;
    private static final int QUERY_GET_ALL = 2;
    private static final int QUERY_CONTAINS = 3;

    public static final String ACTION_PREFERENCES_CHANGE = "org.xjy.android.treasure.PREFERENCES_CHANGE";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_KEYS = "keys";

    public static final String KEYS = "keys";

    private final HashMap<String, HashMap<String, Integer>> mListeners = new HashMap<String, HashMap<String, Integer>>();

    private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        String authority = TreasureContract.getAuthority(getContext());
        mUriMatcher.addURI(authority, "*/" + TreasureContract.QUERY_GET_ALL, QUERY_GET_ALL);
        mUriMatcher.addURI(authority, "*/" + TreasureContract.QUERY_GET, QUERY_GET);
        mUriMatcher.addURI(authority, "*/" + TreasureContract.QUERY_CONTAINS, QUERY_CONTAINS);
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SharedPreferences sp = getContext().getSharedPreferences(uri.getPathSegments().get(0), Context.MODE_PRIVATE);
        Cursor cursor = null;
        switch (mUriMatcher.match(uri)) {
            case QUERY_GET:
                int type = Integer.parseInt(sortOrder);
                switch (type) {
                    case TreasurePreferences.TYPE_STRING:
                        String string = sp.getString(projection[0], null);
                        cursor = new TreasureCursor(string == null ? 0 : 1, string);
                        break;
                    case TreasurePreferences.TYPE_STRING_SET:
                        Set<String> set = sp.getStringSet(projection[0], null);
                        String setJsonStr = null;
                        if (set != null) {
                            setJsonStr = stringSetToJSONArray(set).toString();
                        }
                        cursor = new TreasureCursor(set == null ? 0 : 1, setJsonStr);
                        break;
                    case TreasurePreferences.TYPE_INT:
                        cursor = new TreasureCursor(1, sp.getInt(projection[0], Integer.parseInt(selection)));
                        break;
                    case TreasurePreferences.TYPE_LONG:
                        cursor = new TreasureCursor(1, sp.getLong(projection[0], Long.parseLong(selection)));
                        break;
                    case TreasurePreferences.TYPE_FLOAT:
                        cursor = new TreasureCursor(1, sp.getFloat(projection[0], Float.parseFloat(selection)));
                        break;
                    case TreasurePreferences.TYPE_BOOLEAN:
                        cursor = new TreasureCursor(1, sp.getBoolean(projection[0], Boolean.parseBoolean(selection)) ? 1 : 0);
                        break;
                }
                break;
            case QUERY_GET_ALL:
                Map<String, ?> map = sp.getAll();
                JSONObject json = new JSONObject();
                try {
                    for (Map.Entry<String, ?> entry : map.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (value == null) {
                            json.put(key, JSONObject.NULL);
                        } else if (value instanceof String) {
                            JSONArray array = new JSONArray();
                            array.put(TreasurePreferences.TYPE_STRING);
                            array.put(value);
                            json.put(key, array);
                        } else if (value instanceof Set) {
                            JSONArray array = new JSONArray();
                            array.put(TreasurePreferences.TYPE_STRING_SET);
                            array.put(stringSetToJSONArray((Set<String>) value));
                            json.put(key, array);
                        } else if (value instanceof Integer) {
                            JSONArray array = new JSONArray();
                            array.put(TreasurePreferences.TYPE_INT);
                            array.put(value);
                            json.put(key, array);
                        } else if (value instanceof Long) {
                            JSONArray array = new JSONArray();
                            array.put(TreasurePreferences.TYPE_LONG);
                            array.put(value);
                            json.put(key, array);
                        } else if (value instanceof Float) {
                            JSONArray array = new JSONArray();
                            array.put(TreasurePreferences.TYPE_FLOAT);
                            array.put(value);
                            json.put(key, array);
                        } else if (value instanceof Boolean) {
                            JSONArray array = new JSONArray();
                            array.put(TreasurePreferences.TYPE_BOOLEAN);
                            array.put(value);
                            json.put(key, array);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cursor = new TreasureCursor(1, json.toString());
                break;
            case QUERY_CONTAINS:
                cursor = new TreasureCursor(1,sp.contains(projection[0]) ? 1 : 0);
                break;
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String name = uri.getPathSegments().get(0);
        String keys = values.getAsString(KEYS);
        try {
            JSONArray keyArray = TextUtils.isEmpty(keys) ? null : new JSONArray(keys);
            synchronized (mListeners) {
                HashMap<String, Integer> listeners = mListeners.get(name);
                if (listeners == null) {
                    listeners = new HashMap<String, Integer>();
                    if (keyArray == null) {
                        listeners.put(null, 1);
                    } else {
                        for (int i = keyArray.length() - 1; i >= 0; i--) {
                            listeners.put(keyArray.getString(i), 1);
                        }
                    }
                    mListeners.put(name, listeners);
                } else {
                    if (keyArray == null) {
                        Integer count = listeners.get(null);
                        if (count == null) {
                            listeners.put(null, 1);
                        } else {
                            listeners.put(null, count + 1);
                        }
                    } else {
                        for (int i = keyArray.length() - 1; i >=0; i--) {
                            String key = keyArray.getString(i);
                            Integer count = listeners.get(key);
                            if (count == null) {
                                listeners.put(key, 1);
                            } else {
                                listeners.put(key, count + 1);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String name = uri.getPathSegments().get(0);
        synchronized (mListeners) {
            HashMap<String, Integer> listeners = mListeners.get(name);
            if (listeners != null) {
                String[] keys = selectionArgs == null ? new String[]{null} : selectionArgs;
                for (String key : keys) {
                    Integer count = listeners.get(key);
                    if (count != null) {
                        count = count - 1;
                        if (count > 0) {
                            listeners.put(key, count);
                        } else {
                            listeners.remove(key);
                        }
                    }
                }
                if (listeners.size() == 0) {
                    mListeners.remove(name);
                }
            }
        }
        return 0;
    }

    @SuppressLint("NewApi")
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String name = uri.getPathSegments().get(0);
        SharedPreferences.Editor editor = getContext().getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        boolean clear = Boolean.parseBoolean(uri.getQueryParameter(TreasureContract.PARAM_CLEAR));
        if (clear) {
            editor.clear();
        }
        ArrayList<String> modifiedKeys = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : values.valueSet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                editor.remove(key);
            } else if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (int) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (long) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (float) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (boolean) value);
            }
            modifiedKeys.add(key);
        }
        if (selectionArgs != null) {
            try {
                JSONArray stringSetValueArray = new JSONArray(selection);
                for (int i = 0; i < selectionArgs.length; i++) {
                    editor.putStringSet(selectionArgs[i], jsonArrayToStringSet(stringSetValueArray.getJSONArray(i)));
                    modifiedKeys.add(selectionArgs[i]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (Boolean.parseBoolean(uri.getQueryParameter(TreasureContract.PARAM_IMMEDIATELY))) {
            editor.commit();
        } else {
            editor.apply();
        }

        //notify listeners
        if (modifiedKeys.size() > 0) {
            HashSet<String> keySet = null;
            synchronized (mListeners) {
                HashMap<String, Integer> listeners = mListeners.get(name);
                if (listeners != null) {
                    keySet = new HashSet<String>(listeners.keySet());
                }
            }
            if (keySet != null) {
                if (!keySet.contains(null)) {
                    modifiedKeys.retainAll(keySet);
                }
                if (modifiedKeys.size() > 0) {
                    Intent intent = new Intent(ACTION_PREFERENCES_CHANGE);
                    intent.putExtra(EXTRA_NAME, name);
                    intent.putExtra(EXTRA_KEYS, modifiedKeys);
                    getContext().sendBroadcast(intent);
                }
            }
        }

        return 0;
    }

    public static HashSet<String> jsonArrayToStringSet(JSONArray array) {
        HashSet<String> set = new HashSet<String>();
        try {
            for (int i = array.length() - 1; i >= 0; i--) {
                set.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return set;
    }

    public static JSONArray stringSetToJSONArray(Set<String> set) {
        JSONArray array = new JSONArray();
        for (String s : set) {
            array.put(s);
        }
        return array;
    }
}
