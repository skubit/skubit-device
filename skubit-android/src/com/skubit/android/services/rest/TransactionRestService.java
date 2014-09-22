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
