
package com.skubit.android;

import android.content.Context;
import android.graphics.Typeface;

public final class FontManager {

    public static Typeface LITE;

    protected FontManager(Context ctx) {
        LITE = Typeface.createFromAsset(ctx.getAssets(),
                "fonts/Roboto-Light.ttf");
        ctx = null;
    }
}
