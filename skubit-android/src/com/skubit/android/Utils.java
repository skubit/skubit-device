
package com.skubit.android;

import android.content.Intent;
import android.text.TextUtils;

public class Utils {

    public static Intent createShareIntent(BitcoinUri uri, String defaultMessage) {
        if (TextUtils.isEmpty(uri.message)) {
            uri.message = defaultMessage;
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, uri.message);
        sendIntent.putExtra(Intent.EXTRA_TEXT, uri.toString());
        sendIntent.setType("text/plain");
        return sendIntent;
    }
    
    public static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
