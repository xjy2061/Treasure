package org.xjy.android.treasure.provider;

import android.database.AbstractCursor;

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
        return new String[0];
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
        return (long) mValue;
    }

    @Override
    public float getFloat(int column) {
        return (float) mValue;
    }

    @Override
    public double getDouble(int column) {
        return (double) mValue;
    }

    @Override
    public boolean isNull(int column) {
        return mValue == null;
    }
}
