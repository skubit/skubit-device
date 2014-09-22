
package com.skubit.android.services.rest;

import retrofit.Callback;
import retrofit.http.GET;

import com.skubit.shared.dto.CurrentUserDto;
import com.skubit.shared.dto.UserDto;
import com.skubit.shared.rest.PathParameter;
import com.skubit.shared.rest.ResourcesPath;

public interface AccountsRestService {

    public static final String baseUri = ResourcesPath.ACCOUNTS;

    @GET(baseUri + "/" + PathParameter.USER_PROFILE)
    UserDto getUserProfile();
    

}
