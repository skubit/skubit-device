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
import retrofit.http.GET;
import retrofit.http.PUT;

import com.skubit.shared.dto.BitcoinAddressDto;
import com.skubit.shared.dto.UserDto;
import com.skubit.shared.rest.PathParameter;
import com.skubit.shared.rest.ResourcesPath;

public interface AccountsRestService {

    public static final String baseUri = ResourcesPath.ACCOUNTS;

    @GET(baseUri + "/" + PathParameter.USER_PROFILE)
    void getUserProfile(Callback<UserDto> currentUserDto);

    @PUT(baseUri + "/" + PathParameter.USER_PROFILE)
    void putUserProfile(@Body
    UserDto userProfileDto, Callback<Void> result);
    
    @PUT(baseUri + "/newAddress")
    void newBitcoinAddress(Callback<BitcoinAddressDto> result);
       
    @GET(baseUri + "/addresses/current")
    void getCurrentAddress(Callback<BitcoinAddressDto> result);

}
