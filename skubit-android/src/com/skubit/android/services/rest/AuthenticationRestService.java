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

package com.skubit.android.services.rest;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Query;

import com.skubit.shared.dto.CurrentUserDto;
import com.skubit.shared.dto.LogInResultDto;
import com.skubit.shared.rest.PathParameter;
import com.skubit.shared.rest.ResourcesPath;

public interface AuthenticationRestService {

    public static final String baseUri = ResourcesPath.AUTHENTICATION;

    @POST(baseUri + "/" + PathParameter.CONNECT)
    LogInResultDto postConnect(@Query("mobile")
    boolean mobile,
            @Body
            String code);

    @POST(baseUri + "/" + PathParameter.LOGIN_WITH_COOKIE)
    CurrentUserDto postLoginWithCookie(@Body
    String loggedInCookie);

    @POST(baseUri + "/" + PathParameter.LOGIN_WITH_COOKIE)
    void postLoginWithCookieCallback(@Body
    String loggedInCookie,
            Callback<CurrentUserDto> currentUserDto);
}
