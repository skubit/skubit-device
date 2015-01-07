package com.skubit.android.provider.accounts;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.skubit.android.provider.base.AbstractSelection;

/**
 * Selection for the {@code accounts} table.
 */
public class AccountsSelection extends AbstractSelection<AccountsSelection> {
    @Override
    public Uri uri() {
        return AccountsColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code AccountsCursor} object, which is positioned before the first entry, or null.
     */
    public AccountsCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new AccountsCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null}.
     */
    public AccountsCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null}.
     */
    public AccountsCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }


    public AccountsSelection id(long... value) {
        addEquals("accounts." + AccountsColumns._ID, toObjectArray(value));
        return this;
    }


    public AccountsSelection bitid(String... value) {
        addEquals(AccountsColumns.BITID, value);
        return this;
    }

    public AccountsSelection bitidNot(String... value) {
        addNotEquals(AccountsColumns.BITID, value);
        return this;
    }

    public AccountsSelection bitidLike(String... value) {
        addLike(AccountsColumns.BITID, value);
        return this;
    }

    public AccountsSelection cookie(String... value) {
        addEquals(AccountsColumns.COOKIE, value);
        return this;
    }

    public AccountsSelection cookieNot(String... value) {
        addNotEquals(AccountsColumns.COOKIE, value);
        return this;
    }

    public AccountsSelection cookieLike(String... value) {
        addLike(AccountsColumns.COOKIE, value);
        return this;
    }

    public AccountsSelection date(Long... value) {
        addEquals(AccountsColumns.DATE, value);
        return this;
    }

    public AccountsSelection dateNot(Long... value) {
        addNotEquals(AccountsColumns.DATE, value);
        return this;
    }

    public AccountsSelection dateGt(long value) {
        addGreaterThan(AccountsColumns.DATE, value);
        return this;
    }

    public AccountsSelection dateGtEq(long value) {
        addGreaterThanOrEquals(AccountsColumns.DATE, value);
        return this;
    }

    public AccountsSelection dateLt(long value) {
        addLessThan(AccountsColumns.DATE, value);
        return this;
    }

    public AccountsSelection dateLtEq(long value) {
        addLessThanOrEquals(AccountsColumns.DATE, value);
        return this;
    }
}
