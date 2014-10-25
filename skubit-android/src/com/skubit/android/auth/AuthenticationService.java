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

package com.skubit.android.auth;

import java.io.IOException;

import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.converter.JacksonConverter;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.skubit.android.AccountSettings;
import com.skubit.android.Constants;
import com.skubit.android.services.rest.AuthenticationRestService;
import com.skubit.shared.dto.LogInResultDto;

public class AuthenticationService {

    private static AuthenticationRestService mService;

    private Context mContext;

    public AuthenticationService(Context context) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.SKUBIT_AUTH)
                .setConverter(new JacksonConverter()).build();
        if (Constants.LOG_LEVEL_FULL) {
            restAdapter.setLogLevel(LogLevel.FULL);
        }
        mService = restAdapter.create(AuthenticationRestService.class);
        this.mContext = context;
    }

    private String exchangeCodeForCookie(String account, String code)
            throws UserRecoverableAuthException, IOException,
            GoogleAuthException {
        try {
            LogInResultDto result = mService.postConnect(true, code);// seem to
                                                                     // throw
                                                                     // exception
                                                                     // here
            AccountSettings.get(mContext).saveBitcoinAddress(
                    result.getCurrentUserDto().getUser().getDepositAddress());
            if (result.getErrorMessage() != null) {
                Log.d("skubit", "skubit: "
                        + result.getErrorMessage().getMessage());
            } else {
                return result.getLoggedInCookie();
            }
        } catch (Exception e) {
            e.printStackTrace();
            GoogleAuthUtil.invalidateToken(mContext, code);
            return login(account);

        }
        return null;
    }

    private String getCode(String account) throws UserRecoverableAuthException,
            IOException, GoogleAuthException {
        return GoogleAuthUtil.getToken(mContext, account,
                "oauth2:server:client_id:" + Constants.CLIENT_ID
                        + ":api_scope:" + Constants.API_SCOPE);

    }

    public AuthenticationRestService getRestService() {
        return mService;
    }

    public String login(String account) throws UserRecoverableAuthException,
            IOException, GoogleAuthException {
        String code = getCode(account);
        String cookie = exchangeCodeForCookie(account, code);
        AccountSettings.get(mContext).saveCookie(cookie);
        return cookie;
    }

    public String loginWithCode(String account, String code)
            throws UserRecoverableAuthException, IOException,
            GoogleAuthException {
        String cookie = exchangeCodeForCookie(account, code);
        AccountSettings.get(mContext).saveCookie(cookie);
        return cookie;
    }

    public void signout() {
        final AccountSettings accountSettings = AccountSettings.get(mContext);
        final String account = accountSettings.retrieveGoogleAccount();

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    accountSettings.saveCookie(null);
                    accountSettings.saveGoogleAccount(null);
                    accountSettings.saveBitcoinAddress(null);
                    LocalBroadcastManager.getInstance(mContext)
                            .sendBroadcast(new Intent("account"));

                    if (!TextUtils.isEmpty(account)) {
                        String code = getCode(account);
                        if (!TextUtils.isEmpty(code)) {
                            GoogleAuthUtil.invalidateToken(mContext, code);

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });
        t.start();
    }
}
