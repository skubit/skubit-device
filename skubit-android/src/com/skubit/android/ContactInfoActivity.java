
package com.skubit.android;

import net.skubit.android.R;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

public class ContactInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_info);
        if (savedInstanceState != null) {
            return;
        }

        Fragment fragment = new ContactInfoFragment();
        getFragmentManager().beginTransaction().add(R.id.contact_info_container, fragment).commit();
    }

}
