package com.skubit.android.provider.accounts;

import java.util.Date;

import android.content.ContentResolver;
import android.net.Uri;

import com.skubit.android.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code accounts} table.
 */
public class AccountsContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return AccountsColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, AccountsSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public AccountsContentValues putBitid(String value) {
        mContentValues.put(AccountsColumns.BITID, value);
        return this;
    }

    public AccountsContentValues putBitidNull() {
        mContentValues.putNull(AccountsColumns.BITID);
        return this;
    }


    public AccountsContentValues putCookie(String value) {
        mContentValues.put(AccountsColumns.COOKIE, value);
        return this;
    }

    public AccountsContentValues putCookieNull() {
        mContentValues.putNull(AccountsColumns.COOKIE);
        return this;
    }


    public AccountsContentValues putDate(Long value) {
        mContentValues.put(AccountsColumns.DATE, value);
        return this;
    }

    public AccountsContentValues putDateNull() {
        mContentValues.putNull(AccountsColumns.DATE);
        return this;
    }

}
