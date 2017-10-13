package org.xjy.android.treasure.provider;

import android.database.AbstractCursor;
import android.database.CursorWindow;

class TreasureCursor extends AbstractCursor {
    private static final int FIELD_TYPE_NULL = 0;
    private static final int FIELD_TYPE_INTEGER = 1;
    private static final int FIELD_TYPE_FLOAT = 2;
    private static final int FIELD_TYPE_STRING = 3;
    private static final int FIELD_TYPE_BLOB = 4;

    private int mCount;
    private Object mValue;

    TreasureCursor(int count, Object value) {
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
            return (int) mValue;
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
            return (float) mValue;
        }
        return (double) mValue;
    }

    @Override
    public boolean isNull(int column) {
        return mValue == null;
    }

    @Override
    public int getType(int column) {
        if (mValue instanceof String) {
            return FIELD_TYPE_STRING;
        } else if (mValue instanceof Integer || mValue instanceof Long) {
            return FIELD_TYPE_INTEGER;
        } else if (mValue instanceof Float) {
            return FIELD_TYPE_FLOAT;
        }
        return FIELD_TYPE_NULL;
    }

    @Override
    public void fillWindow(int position, CursorWindow window) {
        if (position < 0 || position >= getCount()) {
            return;
        }
        final int oldPos = getPosition();
        final int numColumns = getColumnCount();
        window.clear();
        window.setStartPosition(position);
        window.setNumColumns(numColumns);
        if (moveToPosition(position)) {
            rowloop: do {
                if (!window.allocRow()) {
                    break;
                }
                for (int i = 0; i < numColumns; i++) {
                    final int type = getType(i);
                    final boolean success;
                    switch (type) {
                        case FIELD_TYPE_NULL:
                            success = window.putNull(position, i);
                            break;

                        case FIELD_TYPE_INTEGER:
                            success = window.putLong(getLong(i), position, i);
                            break;

                        case FIELD_TYPE_FLOAT:
                            success = window.putDouble(getDouble(i), position, i);
                            break;

                        case FIELD_TYPE_BLOB: {
                            final byte[] value = getBlob(i);
                            success = value != null ? window.putBlob(value, position, i)
                                    : window.putNull(position, i);
                            break;
                        }

                        default: // assume value is convertible to String
                        case FIELD_TYPE_STRING: {
                            final String value = getString(i);
                            success = value != null ? window.putString(value, position, i)
                                    : window.putNull(position, i);
                            break;
                        }
                    }
                    if (!success) {
                        window.freeLastRow();
                        break rowloop;
                    }
                }
                position += 1;
            } while (moveToNext());
        }
        moveToPosition(oldPos);
    }
}
