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

import com.skubit.shared.rest.PathParameter;
import com.skubit.shared.rest.ResourcesPath;

public interface TransactionRestService {

    public static final String baseUri = ResourcesPath.TRANSACTIONS;
    
    @GET(baseUri + "/" + PathParameter.BALANCE)
    void getBalance(Callback<String> balance);
    
}
