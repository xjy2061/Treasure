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
import java.util.List;
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

    private Context mContext;

    private final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private final HashMap<String, HashMap<String, Integer>> mListeners = new HashMap<>();

    private HashMap<String, HashMap<String, Object>> mMemoryStorage;

    @Override
    public boolean onCreate() {
        mContext = getContext();

        String authority = TreasureContract.getAuthority(mContext);
        mUriMatcher.addURI(authority, "*/*/" + TreasureContract.QUERY_GET_ALL, QUERY_GET_ALL);
        mUriMatcher.addURI(authority, "*/*/" + TreasureContract.QUERY_GET, QUERY_GET);
        mUriMatcher.addURI(authority, "*/*/" + TreasureContract.QUERY_CONTAINS, QUERY_CONTAINS);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        List<String> paths = uri.getPathSegments();
        String name = paths.get(0);
        boolean inMemory = Integer.parseInt(paths.get(1)) == TreasurePreferences.MODE_IN_MEMORY;
        switch (mUriMatcher.match(uri)) {
            case QUERY_GET:
                int type = Integer.parseInt(sortOrder);
                switch (type) {
                    case TreasurePreferences.TYPE_STRING:
                        String string;
                        if (inMemory) {
                            string = (String) getFromMemoryPreferences(name, projection[0]);
                        } else {
                            string = mContext.getSharedPreferences(name, Context.MODE_PRIVATE).getString(projection[0], null);
                        }
                        cursor = new TreasureCursor(string == null ? 0 : 1, string);
                        break;
                    case TreasurePreferences.TYPE_STRING_SET:
                        Set<String> set;
                        if (inMemory) {
                            set = (Set<String>) getFromMemoryPreferences(name, projection[0]);
                        } else {
                            set = mContext.getSharedPreferences(name, Context.MODE_PRIVATE).getStringSet(projection[0], null);
                        }
                        String setJsonStr = null;
                        if (set != null) {
                            setJsonStr = stringSetToJSONArray(set).toString();
                        }
                        cursor = new TreasureCursor(set == null ? 0 : 1, setJsonStr);
                        break;
                    case TreasurePreferences.TYPE_INT:
                        int intVal;
                        if (inMemory) {
                            Object valObj = getFromMemoryPreferences(name, projection[0]);
                            intVal = valObj == null ?  Integer.parseInt(selection) : (int) valObj;
                        } else {
                            intVal = mContext.getSharedPreferences(name, Context.MODE_PRIVATE).getInt(projection[0], Integer.parseInt(selection));
                        }
                        cursor = new TreasureCursor(1, intVal);
                        break;
                    case TreasurePreferences.TYPE_LONG:
                        long longVal;
                        if (inMemory) {
                            Object valObj = getFromMemoryPreferences(name, projection[0]);
                            longVal = valObj == null ? Long.parseLong(selection) : (long) valObj;
                        } else {
                            longVal = mContext.getSharedPreferences(name, Context.MODE_PRIVATE).getLong(projection[0], Long.parseLong(selection));
                        }
                        cursor = new TreasureCursor(1, longVal);
                        break;
                    case TreasurePreferences.TYPE_FLOAT:
                        float floatVal;
                        if (inMemory) {
                            Object valObj = getFromMemoryPreferences(name, projection[0]);
                            floatVal = valObj == null ? Float.parseFloat(selection) : (float) valObj;
                        } else {
                            floatVal = mContext.getSharedPreferences(name, Context.MODE_PRIVATE).getFloat(projection[0], Float.parseFloat(selection));
                        }
                        cursor = new TreasureCursor(1, floatVal);
                        break;
                    case TreasurePreferences.TYPE_BOOLEAN:
                        boolean booleanVal;
                        if (inMemory) {
                            Object valObj = getFromMemoryPreferences(name, projection[0]);
                            booleanVal = valObj == null ? Boolean.parseBoolean(selection) : (boolean) valObj;
                        } else {
                            booleanVal = mContext.getSharedPreferences(name, Context.MODE_PRIVATE).getBoolean(projection[0], Boolean.parseBoolean(selection));
                        }
                        cursor = new TreasureCursor(1, booleanVal ? 1 : 0);
                        break;
                }
                break;
            case QUERY_GET_ALL:
                Map<String, ?> map;
                if (inMemory) {
                    synchronized (this) {
                        map = new HashMap<>(getMemoryPreferences(name));
                    }
                } else {
                    map = mContext.getSharedPreferences(name, Context.MODE_PRIVATE).getAll();
                }
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
                boolean contains;
                if (inMemory) {
                    synchronized (this) {
                        contains = getMemoryPreferences(name).containsKey(projection[0]);
                    }
                } else {
                    contains = mContext.getSharedPreferences(name, Context.MODE_PRIVATE).contains(projection[0]);
                }
                cursor = new TreasureCursor(1, contains ? 1 : 0);
                break;
        }
        return cursor;
    }

    @Override
    public String getType(@SuppressWarnings("NullableProblems") Uri uri) {
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
                    listeners = new HashMap<>();
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

    @SuppressLint("ApplySharedPref")
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        List<String> paths = uri.getPathSegments();
        String name = paths.get(0);
        boolean inMemory = Integer.parseInt(paths.get(1)) == TreasurePreferences.MODE_IN_MEMORY;
        boolean clear = Boolean.parseBoolean(uri.getQueryParameter(TreasureContract.PARAM_CLEAR));
        ArrayList<String> modifiedKeys = new ArrayList<>();
        if (inMemory) {
            synchronized (this) {
                HashMap<String, Object> memoryPreferences = getMemoryPreferences(name);
                if (clear) {
                    memoryPreferences.clear();
                }
                for (Map.Entry<String, Object> entry : values.valueSet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value == null) {
                        memoryPreferences.remove(key);
                    } else {
                        memoryPreferences.put(key, value);
                    }
                    modifiedKeys.add(key);
                }
                if (selectionArgs != null) {
                    try {
                        JSONArray stringSetValueArray = new JSONArray(selection);
                        for (int i = 0; i < selectionArgs.length; i++) {
                            memoryPreferences.put(selectionArgs[i], jsonArrayToStringSet(stringSetValueArray.getJSONArray(i)));
                            modifiedKeys.add(selectionArgs[i]);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            SharedPreferences.Editor editor = mContext.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
            if (clear) {
                editor.clear();
            }
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
        }

        //notify listeners
        if (modifiedKeys.size() > 0) {
            HashSet<String> keySet = null;
            synchronized (mListeners) {
                HashMap<String, Integer> listeners = mListeners.get(name);
                if (listeners != null) {
                    keySet = new HashSet<>(listeners.keySet());
                }
            }
            if (keySet != null) {
                if (!keySet.contains(null)) {
                    modifiedKeys.retainAll(keySet);
                }
                if (modifiedKeys.size() > 0) {
                    Intent intent = new Intent(ACTION_PREFERENCES_CHANGE);
                    intent.setPackage(mContext.getPackageName());
                    intent.putExtra(EXTRA_NAME, name);
                    intent.putStringArrayListExtra(EXTRA_KEYS, modifiedKeys);
                    mContext.sendBroadcast(intent);
                }
            }
        }

        return 0;
    }

    private synchronized Object getFromMemoryPreferences(String name, Object key) {
        return getMemoryPreferences(name).get(key);
    }

    private HashMap<String, Object> getMemoryPreferences(String name) {
        if (mMemoryStorage == null) {
            mMemoryStorage = new HashMap<>();
        }
        HashMap<String, Object> memoryPreferences = mMemoryStorage.get(name);
        if (memoryPreferences == null) {
            memoryPreferences = new HashMap<>();
            mMemoryStorage.put(name, memoryPreferences);
        }
        return memoryPreferences;
    }

    public static HashSet<String> jsonArrayToStringSet(JSONArray array) {
        HashSet<String> set = new HashSet<>();
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
