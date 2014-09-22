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

package com.skubit.android.billing;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BillingService extends Service {

    private BillingServiceBinder mBinder;

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) {
            mBinder = new BillingServiceBinder(this);
        }
        return mBinder;
    }

}