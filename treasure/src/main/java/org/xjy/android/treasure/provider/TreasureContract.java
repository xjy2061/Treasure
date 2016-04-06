package org.xjy.android.treasure.provider;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import org.xjy.android.treasure.R;

public final class TreasureContract {
    public static final String QUERY_GET = "get";
    public static final String QUERY_GET_ALL = "getall";
    public static final String QUERY_CONTAINS = "contains";
    public static final String PARAM_CLEAR = "clear";
    public static final String PARAM_IMMEDIATELY = "immediately";
    public static final String UPDATE = "update";
    public static final String REGISTER = "register";
    public static final String UNREGISTER = "unregister";

    private static String sAuthority;
    private static Uri sAuthorityUri;

    public static String getAuthority(Context context) {
        if (TextUtils.isEmpty(sAuthority)) {
            sAuthority = context.getString(R.string.org_xjy_android_treasure_authority);
        }
        return sAuthority;
    }

    public static Uri getAuthorityUri(Context context) {
        if (sAuthorityUri == null) {
            sAuthorityUri = Uri.parse("content://" + getAuthority(context));
        }
        return sAuthorityUri;
    }
}
