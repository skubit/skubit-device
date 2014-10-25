package com.skubit.android.osl;

import java.io.IOException;

import net.skubit.android.R;
import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayLicensesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.licenses_activity);
        this.setTitle("Open Source Licenses");
        TextView licensesText = (TextView) this.findViewById(R.id.licenses_test);
        
        AssetManager assetManager = getAssets();
        String text;
        try {
            text = toString(assetManager.open("NOTICE.txt"));
        } catch (IOException e) {
           return;
        }
        licensesText.setText(text);
    }

    private static String toString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
