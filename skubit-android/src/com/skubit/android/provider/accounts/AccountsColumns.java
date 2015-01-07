package com.skubit.android.provider.accounts;

import android.net.Uri;
import android.provider.BaseColumns;

import com.skubit.android.provider.KeyProvider;
import com.skubit.android.provider.accounts.AccountsColumns;

/**
 * Columns for the {@code accounts} table.
 */
public class AccountsColumns implements BaseColumns {
    public static final String TABLE_NAME = "accounts";
    public static final Uri CONTENT_URI = Uri.parse(KeyProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = new String(BaseColumns._ID);

    public static final String BITID = "bitid";

    public static final String COOKIE = "cookie";

    public static final String DATE = "date";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            BITID,
            COOKIE,
            DATE
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c == BITID || c.contains("." + BITID)) return true;
            if (c == COOKIE || c.contains("." + COOKIE)) return true;
            if (c == DATE || c.contains("." + DATE)) return true;
        }
        return false;
    }

}
