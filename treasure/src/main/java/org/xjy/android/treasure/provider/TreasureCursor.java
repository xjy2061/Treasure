package org.xjy.android.treasure.provider;

import android.annotation.TargetApi;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.os.Build;

public class TreasureCursor extends AbstractCursor {
    private int mCount;
    private Object mValue;

    public TreasureCursor(int count, Object value) {
        mCount = count;
        mValue = value;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"value"};
    }

    @Override
    public String getString(int column) {
        return (String) mValue;
    }

    @Override
    public short getShort(int column) {
        return (short) mValue;
    }

    @Override
    public int getInt(int column) {
        return (int) mValue;
    }

    @Override
    public long getLong(int column) {
        if (mValue instanceof Integer) {
            int i = (int) mValue;
            return i;
        }
        return (long) mValue;
    }

    @Override
    public float getFloat(int column) {
        return (float) mValue;
    }

    @Override
    public double getDouble(int column) {
        if (mValue instanceof Float) {
            float f = (float) mValue;
            return f;
        }
        return (double) mValue;
    }

    @Override
    public boolean isNull(int column) {
        return mValue == null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public int getType(int column) {
        if (mValue instanceof String) {
            return Cursor.FIELD_TYPE_STRING;
        } else if (mValue instanceof Integer || mValue instanceof Long) {
            return Cursor.FIELD_TYPE_INTEGER;
        } else if (mValue instanceof Float) {
            return Cursor.FIELD_TYPE_FLOAT;
        }
        return Cursor.FIELD_TYPE_NULL;
    }
}
