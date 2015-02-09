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
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

import com.skubit.shared.dto.ApplicationsListDto;
import com.skubit.shared.dto.SkuDetailsDto;
import com.skubit.shared.dto.SkuDetailsListDto;
import com.skubit.shared.rest.PathParameter;
import com.skubit.shared.rest.ResourcesPath;

public interface InventoryRestService {

    public static final String baseUri = ResourcesPath.INVENTORY_API;

    @GET(baseUri + "/" + PathParameter.INVENTORY_COUNT)
    Integer getApplicationCount(@Path("application")
    String application);

    @GET(baseUri + "/applications")
    public ApplicationsListDto getApplications(
            @Query("vendorId")
            String vendorId, @Query("limit")
            int limit,
            @Query("offset")
            int offset, @Query("cursor")
            String cursor);

    @GET(baseUri + "/" + PathParameter.SKUS_SKUDETAILS)
    public void getSkuDetails(@Path("application")
    String application,
            @Path("productId")
            String productId,
            Callback<SkuDetailsDto> skuDetailsDto);

    @GET(baseUri + "/" + PathParameter.SKUS_LIST)
    SkuDetailsListDto getSkuDetailsByIds(
            @Path("application")
            String application,
            @Query("ids")
            String productIds);

    @GET(baseUri + "/" + PathParameter.SKUS_COUNT)
    Integer getSkuDetailsCount(@Path("application")
    String application);
}
