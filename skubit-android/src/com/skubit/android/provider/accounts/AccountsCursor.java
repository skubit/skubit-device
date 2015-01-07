package com.skubit.android.provider.accounts;

import java.util.Date;

import android.database.Cursor;

import com.skubit.android.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code accounts} table.
 */
public class AccountsCursor extends AbstractCursor {
    public AccountsCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Get the {@code bitid} value.
     * Can be {@code null}.
     */
    public String getBitid() {
        Integer index = getCachedColumnIndexOrThrow(AccountsColumns.BITID);
        return getString(index);
    }

    /**
     * Get the {@code cookie} value.
     * Can be {@code null}.
     */
    public String getCookie() {
        Integer index = getCachedColumnIndexOrThrow(AccountsColumns.COOKIE);
        return getString(index);
    }

    /**
     * Get the {@code date} value.
     * Can be {@code null}.
     */
    public Long getDate() {
        return getLongOrNull(AccountsColumns.DATE);
    }
}
