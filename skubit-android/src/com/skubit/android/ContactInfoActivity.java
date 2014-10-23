/**
 * Copyright 2014 Skubit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.skubit.android;

import com.skubit.android.R;
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
