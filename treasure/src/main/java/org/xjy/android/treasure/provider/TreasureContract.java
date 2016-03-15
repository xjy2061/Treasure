package org.xjy.android.treasure.provider;

import android.net.Uri;

public final class TreasureContract {
    public static final String AUTHORITY = "org.xjy.android.treasure";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final String QUERY_GET = "get";
    public static final String QUERY_GET_ALL = "getall";
    public static final String QUERY_CONTAINS = "contains";
    public static final String UPDATE = "update";
    public static final String REGISTER = "register";
    public static final String UNREGISTER = "unregister";
}
