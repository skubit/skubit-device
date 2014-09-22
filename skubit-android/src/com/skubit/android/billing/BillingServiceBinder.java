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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.common.base.Joiner;
import com.skubit.android.AccountSettings;
import com.skubit.android.PurchaseActivity;
import com.skubit.android.SkubitAndroidActivity;
import com.skubit.android.services.InventoryService;
import com.skubit.android.services.PurchaseService;
import com.skubit.android.services.rest.InventoryRestService;
import com.skubit.android.services.rest.PurchaseRestService;
import com.skubit.shared.dto.InAppPurchaseDataDto;
import com.skubit.shared.dto.InAppPurchaseDataListDto;
import com.skubit.shared.dto.SkuDetailsDto;
import com.skubit.shared.dto.SkuDetailsListDto;

public class BillingServiceBinder extends IBillingService.Stub {

    private static final String TAG = "BillingService";

    public static String hash(byte[] message) {
        int flag = Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(message);
            return Base64.encodeToString(md.digest(), flag);
        } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
        }
        return null;
    }

    private AccountManager mAccountManager;

    private AccountSettings mAccountSettings;

    private Context mContext;

    private PackageManager mPackageManager;

    public BillingServiceBinder() {
    }

    public BillingServiceBinder(Context context) {
        this.mContext = context;
        mPackageManager = mContext.getPackageManager();
        mAccountSettings = AccountSettings.get(context);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public int consumePurchase(int apiVersion, String packageName,
            String purchaseToken) throws RemoteException {

        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(purchaseToken)) {
            Log.d(TAG, "Missing required parameter");
            return BillingResponseCodes.RESULT_DEVELOPER_ERROR;
        }

        if (apiVersion != 1) {
            Log.d(TAG, "Unsupported API: " + apiVersion);
            return BillingResponseCodes.RESULT_BILLING_UNAVAILABLE;
        }

        int packValidate = validatePackageIsOwnedByCaller(packageName);
        if (packValidate != BillingResponseCodes.RESULT_OK) {
            Log.d(TAG, "Package is not owned by caller");
            return packValidate;
        }
        // TODO

        return 0;
    }

    private boolean exists(String accountName, String accountType) {
        if (TextUtils.isEmpty(accountName)) {
            return false;
        }
        Account[] accounts = mAccountManager.getAccountsByType(accountType);
        if (accounts != null && accounts.length > 0) {
            for (Account account : accounts) {
                if (account.name.equals(accountName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Bundle getBuyIntent(int apiVersion, String packageName, String sku,
            String type, String developerPayload) throws RemoteException {

        Bundle bundle = new Bundle();
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(sku)
                || TextUtils.isEmpty(type)) {
            Log.d(TAG, "Missing required parameter");
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_DEVELOPER_ERROR);
            return bundle;
        }

        if (apiVersion != 1) {
            Log.d(TAG, "Unsupported API: " + apiVersion);
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_BILLING_UNAVAILABLE);
            return bundle;
        }

        if ((!TextUtils.equals(type, "inapp"))
                && (!TextUtils.equals(type, "subs"))
                && (!TextUtils.equals(type, "donate"))) {
            Log.d(TAG, "Incorrect billing type: " + type);
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_BILLING_UNAVAILABLE);
            return bundle;
        }

        int packValidate = validatePackageIsOwnedByCaller(packageName);
        if (packValidate != BillingResponseCodes.RESULT_OK) {
            Log.d(TAG, "Package is not owned by caller");
            bundle.putInt("RESPONSE_CODE", packValidate);
            return bundle;
        }

        /**
         * DO we already own this product? method: userId, packageName,
         * productId bundle.putInt("RESPONSE_CODE",
         * BillingResponseCodes.RESULT_ITEM_ALREADY_OWNED)
         */
        Intent purchaseIntent = makePurchaseIntent(apiVersion, packageName,
                sku, developerPayload);
        if (purchaseIntent == null) {
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_DEVELOPER_ERROR);
            return bundle;
        }
        PendingIntent pending = PendingIntent.getActivity(mContext, 0,
                purchaseIntent, 0);
        bundle.putParcelable("BUY_INTENT", pending);

        bundle.putInt("RESPONSE_CODE", BillingResponseCodes.RESULT_OK);
        return bundle;
    }

    private Account getCurrentGoogleAccount() {
        return new Account(mAccountSettings.retrieveGoogleAccount(),
                "com.google");
    }

    private PackageInfo getPackageInfo(String packageName) {
        try {
            return mPackageManager.getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {

        }
        return null;
    }

    @Override
    public Bundle getPurchases(int apiVersion, String packageName, String type,
            String continuationToken) throws RemoteException {

        Bundle bundle = new Bundle();
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(type)) {
            Log.d(TAG, "Missing required parameter");
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_DEVELOPER_ERROR);
            return bundle;
        }

        if (apiVersion != 1) {
            Log.d(TAG, "Unsupported API: " + apiVersion);
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_BILLING_UNAVAILABLE);
            return bundle;
        }

        if ((!TextUtils.equals(type, "inapp"))
                && (!TextUtils.equals(type, "subs"))
                && (!TextUtils.equals(type, "donate"))) {
            Log.d(TAG, "Incorrect billing type: " + type);
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_BILLING_UNAVAILABLE);
            return bundle;
        }

        int packValidate = validatePackageIsOwnedByCaller(packageName);
        if (packValidate != BillingResponseCodes.RESULT_OK) {
            Log.d(TAG, "Package is not owned by caller");
            bundle.putInt("RESPONSE_CODE", packValidate);
            return bundle;
        }
        String accountName = getCurrentGoogleAccount().name;
        PurchaseRestService service = new PurchaseService(
                getCurrentGoogleAccount(), mContext).getRestService();

        ArrayList<String> ids = new ArrayList<String>();
        ArrayList<String> data = new ArrayList<String>();
        ArrayList<String> sigs = new ArrayList<String>();

        InAppPurchaseDataListDto list = service.getPurchaseDatas(packageName,
                500, 0, continuationToken, true, 0);
        if (list != null) {
            for (InAppPurchaseDataDto dto : list.getItems()) {
                ids.add(dto.getId());
                data.add(dto.getMessage());
                sigs.add(dto.getSignature());
            }
        }

        bundle.putString("INAPP_CONTINUATION_TOKEN", list.getNextLink());

        bundle.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", ids);
        bundle.putStringArrayList("INAPP_PURCHASE_DATA_LIST", data);
        bundle.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", sigs);
        return bundle;
    }

    @Override
    public Bundle getSkuDetails(int apiVersion, String packageName,
            String type, Bundle skusBundle) throws RemoteException {
        Bundle bundle = new Bundle();
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(type)) {
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_DEVELOPER_ERROR);
            return bundle;
        }

        if (apiVersion != 1) {
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_BILLING_UNAVAILABLE);
            return bundle;
        }

        if (skusBundle == null || !skusBundle.containsKey("ITEM_ID_LIST")) {
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_DEVELOPER_ERROR);
            return bundle;
        }

        int packValidate = validatePackageIsOwnedByCaller(packageName);
        if (packValidate != BillingResponseCodes.RESULT_OK) {
            bundle.putInt("RESPONSE_CODE", packValidate);
            return bundle;
        }

        if ((!TextUtils.equals(type, "inapp"))
                && (!TextUtils.equals(type, "subs"))) {
            bundle.putInt("RESPONSE_CODE",
                    BillingResponseCodes.RESULT_BILLING_UNAVAILABLE);
            return bundle;
        }

        InventoryRestService service = new InventoryService(
                getCurrentGoogleAccount(), mContext).getRestService();

        ArrayList<String> itemIds = skusBundle
                .getStringArrayList("ITEM_ID_LIST");
        SkuDetailsListDto skuDetailsListDto = null;
        try {
            skuDetailsListDto = service.getSkuDetailsByIds(packageName, Joiner
                    .on(",").join(itemIds));
        } catch (Exception e1) {
            e1.printStackTrace();
            bundle.putInt("RESPONSE_CODE", BillingResponseCodes.RESULT_ERROR);
        }
        ArrayList<String> details = new ArrayList<String>();
        for (SkuDetailsDto skuDetailsDto : skuDetailsListDto.getItems()) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("productId", skuDetailsDto.getProductId());
                jo.put("type", skuDetailsDto.getType().name());
                jo.put("price", String.valueOf(skuDetailsDto.getSatoshi()));
                jo.put("title", skuDetailsDto.getTitle());
                jo.put("description", skuDetailsDto.getDescription());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            details.add(jo.toString());
        }

        bundle.putStringArrayList("DETAILS_LIST", details);
        bundle.putInt("RESPONSE_CODE", BillingResponseCodes.RESULT_OK);

        return bundle;
    }

    @Override
    public boolean isAuthenticated(boolean startDialog) throws RemoteException {
        String googleAccount = mAccountSettings.retrieveGoogleAccount();

        if (!exists(googleAccount, "com.google")) {
            if (startDialog) {
                Intent mainIntent = new Intent(mContext, SkubitAndroidActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(mainIntent);
            }
            return false;
        }
        return true;
    }

    @Override
    public int isBillingSupported(int apiVersion, String packageName,
            String type) throws RemoteException {

        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(type)) {
            Log.d(TAG, "Missing required parameter");
            return BillingResponseCodes.RESULT_DEVELOPER_ERROR;
        }

        if (apiVersion != 1) {
            return BillingResponseCodes.RESULT_BILLING_UNAVAILABLE;
        }

        int packValidate = validatePackageIsOwnedByCaller(packageName);
        if (packValidate != BillingResponseCodes.RESULT_OK) {
            Log.d(TAG, "Package is not owned by caller");
            return packValidate;
        }

        if ((!TextUtils.equals(type, "inapp"))
                && (!TextUtils.equals(type, "subs"))
                && (!TextUtils.equals(type, "donate"))) {
            return BillingResponseCodes.RESULT_BILLING_UNAVAILABLE;
        }

        return BillingResponseCodes.RESULT_OK;
    }

    public Intent makePurchaseIntent(int apiVersion, String packageName,
            String sku, String devPayload) {

        Account googleAccount = getCurrentGoogleAccount();

        PurchaseData info = new PurchaseData();
        PackageInfo packageInfo = getPackageInfo(packageName);
        if (packageInfo == null) {
            Log.d(TAG, "Package info not found");
            return null;
        }
        if (packageInfo.signatures == null
                || packageInfo.signatures.length == 0) {
            Log.d(TAG, "Missing package signature");
            return null;
        }
        info.signatureHash = hash(packageInfo.signatures[0].toByteArray());
        info.apiVersion = apiVersion;
        info.versionCode = packageInfo.versionCode;
        info.sku = sku;
        info.developerPayload = devPayload;
        info.packageName = packageName;
        return PurchaseActivity.newIntent(googleAccount, info);
    }

    private int validatePackageIsOwnedByCaller(String packageName) {
        String[] packages = mPackageManager.getPackagesForUid(Binder
                .getCallingUid());
        if (packages != null) {
            for (String pack : packages) {
                if (packageName.equals(pack)) {
                    return BillingResponseCodes.RESULT_OK;
                }
            }
        }
        return BillingResponseCodes.RESULT_DEVELOPER_ERROR;
    }
}
