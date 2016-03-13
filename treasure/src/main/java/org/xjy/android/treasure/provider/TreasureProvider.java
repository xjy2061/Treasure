package org.xjy.android.treasure.provider;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xjy.android.treasure.TreasurePreferences;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TreasureProvider extends ContentProvider {
    public static final int QUERY_GET = 1;
    public static final int QUERY_GET_ALL = 2;
    public static final int QUERY_CONTAINS = 3;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(TreasureContract.AUTHORITY, "*/" + TreasureContract.QUERY_GET_ALL, QUERY_GET_ALL);
        sUriMatcher.addURI(TreasureContract.AUTHORITY, "*/" + TreasureContract.QUERY_GET, QUERY_GET);
        sUriMatcher.addURI(TreasureContract.AUTHORITY, "*/" + TreasureContract.QUERY_CONTAINS, QUERY_CONTAINS);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SharedPreferences sp = getContext().getSharedPreferences(uri.getPathSegments().get(0), Context.MODE_PRIVATE);
        Cursor cursor = null;
        switch (sUriMatcher.match(uri)) {
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
                            JSONArray jsonArray = new JSONArray();
                            for (String s : set) {
                                jsonArray.put(s);
                            }
                            setJsonStr = jsonArray.toString();
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
                JSONObject jsonObject = new JSONObject();
                try {
                    for (Map.Entry<String, ?> entry : map.entrySet()) {
                        jsonObject.put(entry.getKey(), entry.getValue());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cursor = new TreasureCursor(1, jsonObject.toString());
                break;
            case QUERY_CONTAINS:
                cursor = new TreasureCursor(1,sp.contains(projection[0]) ? 1 : 0);
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @SuppressLint("NewApi")
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(uri.getPathSegments().get(0), Context.MODE_PRIVATE).edit();
        boolean clear = Boolean.parseBoolean(uri.getQueryParameter("clear"));
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
        }
        if (selection != null) {
            HashSet<String> set = null;
            if (selectionArgs != null) {
                set = new HashSet<String>();
                for (int i = 0; i < selectionArgs.length; i++) {
                    set.add(selectionArgs[i]);
                }
            }
            editor.putStringSet(selection, set);
        }
        if (Boolean.parseBoolean(uri.getQueryParameter("immediately"))) {
            editor.commit();
        } else {
            editor.apply();
        }
        return 0;
    }
}
