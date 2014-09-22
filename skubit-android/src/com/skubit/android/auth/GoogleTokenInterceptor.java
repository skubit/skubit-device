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

import retrofit.RequestInterceptor;
import android.accounts.Account;
import android.content.Context;

import com.skubit.android.AccountSettings;

public class GoogleTokenInterceptor implements RequestInterceptor {

    private final Account mAccount;

    private Context mContext;

    public GoogleTokenInterceptor(Account account, Context context) {
        mAccount = account;
        this.mContext = context;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Cookie", "skubit="
                + AccountSettings.get(mContext).retrieveCookie());
        // If cookie expired, get another
        String token = null;
        // TODO handle refresh
        /*
         * try { token = GoogleAuthUtil.getToken(mContext, mAccount.name, type);
         * request.addQueryParam("access_token", token); } catch
         * (UserRecoverableAuthException e) { e.printStackTrace(); } catch
         * (IOException e) { e.printStackTrace(); } catch (GoogleAuthException
         * e) { e.printStackTrace(); }
         */
    }

}
