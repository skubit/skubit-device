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

import android.content.Context;
import android.content.SharedPreferences;

public class AccountSettings {

    private static final String BITCOIN_ADDRESS = "bitcoinAddress";

    private static final String COOKIE = "cookie";

    private static final String GOOGLE_ACCOUNT = "googleAccount";
    
    private static final String INDEX = "index";

    private static volatile AccountSettings sInstance = null;

    public static AccountSettings get(Context context) {
        if (sInstance == null) {
            synchronized (AccountSettings.class) {
                if (sInstance == null) {
                    sInstance = new AccountSettings(context);
                }
            }
        }
        return sInstance;
    }

    private Context context;

    private AccountSettings(Context context) {
        this.context = context;
    }

    public int getCurrentIndex() {
        return retrieveIntPreference(INDEX);
    }

    public String retrieveBitcoinAddress() {
        return retrieveStringPreference(BITCOIN_ADDRESS);
    }

    public String retrieveCookie() {
        return retrieveStringPreference(COOKIE);
    }
    
    public String retrieveGoogleAccount() {
        return retrieveStringPreference(GOOGLE_ACCOUNT);
    }
    
    private int retrieveIntPreference(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    private String retrieveStringPreference(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }
    
    public void saveBitcoinAddress(String address) {
        saveStringPreference(BITCOIN_ADDRESS, address);
    }

    public void saveCookie(String cookie) {
        saveStringPreference(COOKIE, cookie);
    }

    public void saveGoogleAccount(String googleAccount) {
        saveStringPreference(GOOGLE_ACCOUNT, googleAccount);
    }

    private void saveIntPreference(String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private void saveStringPreference(String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    
    public void setCurrentIndex(int index) {
        saveIntPreference(INDEX, index);
    }
}
