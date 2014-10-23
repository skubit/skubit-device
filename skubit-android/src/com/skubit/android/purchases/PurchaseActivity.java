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

package com.skubit.android.purchases;

import java.text.MessageFormat;

import com.skubit.android.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.skubit.android.FontManager;
import com.skubit.android.billing.PurchaseData;
import com.skubit.android.currencies.Bitcoin;
import com.skubit.android.currencies.Satoshi;
import com.skubit.android.services.PurchaseService;
import com.skubit.android.services.rest.PurchaseRestService;
import com.skubit.shared.dto.ErrorMessage;
import com.skubit.shared.dto.PurchaseDataDto;
import com.skubit.shared.dto.PurchaseDataStatus;
import com.skubit.shared.dto.SkuDetailsDto;

public final class PurchaseActivity extends BasePurchaseActivity {

    public static Intent newIntent(Account googleAccount, PurchaseData data, String packageName) {
        Intent intent = new Intent(data.sku);

        Parcel parcel = Parcel.obtain();
        data.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        intent.putExtra("PurchaseActivity.account", googleAccount);
        intent.putExtra("PurchaseActivity.purchaseData", parcel.marshall());

        intent.setClassName(packageName,
                PurchaseActivity.class.getName());

        return intent;
    }

    private TextView mPrice;

    @Override
    protected void getSkuDetails() {
        mInventoryService.getSkuDetails(mPurchaseData.packageName,
                mPurchaseData.sku, new Callback<SkuDetailsDto>() {

                    @Override
                    public void failure(RetrofitError error) {
                        error.printStackTrace();
                        showMessage("Unable to find the SKU you requested");
                    }

                    @Override
                    public void success(SkuDetailsDto skuDetailsDto,
                            Response response) {
                        hideLoading();
                        if (PurchaseDataStatus.COMPLETED.equals(skuDetailsDto
                                .getPurchaseDataStatus())) {
                            mPrice.setText("Already Purchased");
                            mPurchaseBtn.setText("Close");
                            mPurchaseBtn.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    finish();
                                }
                            });
                        } else {
                            String price = new Bitcoin(new Satoshi(skuDetailsDto.getSatoshi()))
                                    .getDisplay();
                            mPrice.setText(price + " BTC");
                            mPurchaseBtn.setText("BUY");
                            mPurchaseBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showLoading();

                                    Thread t = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            PurchaseDataDto request = new PurchaseDataDto();
                                            request.setUserId(mAccount.name);
                                            request.setDeveloperPayload(mPurchaseData.developerPayload);
                                            putPurchaseData(request);
                                            // send developer payload
                                        }

                                    });
                                    t.start();
                                }
                            });
                        }
                        String message = MessageFormat.format("{0} ({1})",
                                skuDetailsDto.getDescription(), skuDetailsDto.getTitle());
                        mTitle.setText(message);
                        setResult(0, new Intent().putExtra("RESPONSE_CODE", 0));
                    }

                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.purchase_activity_frame);
        super.onCreate(savedInstanceState);

        mPrice = (TextView) findViewById(R.id.price);
        mPrice.setTypeface(FontManager.LITE);
    }

    @Override
    protected void putPurchaseData(PurchaseDataDto request) {
        PurchaseRestService service = new PurchaseService(mAccount, this)
                .getRestService();

        service.postPurchaseData(mPurchaseData.packageName, mPurchaseData.sku,
                request, new Callback<PurchaseDataDto>() {

                    @Override
                    public void failure(RetrofitError error) {
                        error.printStackTrace();
                        String json = new String(((TypedByteArray) error.getResponse().getBody())
                                .getBytes());
                        ErrorMessage message = new Gson().fromJson(json, ErrorMessage.class);

                        int responseCode = message.getCode();
                        if (message.getCode() == 9000) {
                            showMessage("Insufficient funds");
                        } else if (responseCode == 9001) {
                            showMessage("Missing contact info");
                        } else if (responseCode == 9002) {
                            showMessage("Invalid request");
                        }
                    }

                    @Override
                    public void success(final PurchaseDataDto purchaseDataDto,
                            Response response) {
                        showMessage("Purchase successful");
                        setResult(0, new Intent().putExtra("RESPONSE_CODE", 0));
                    }

                });
    }

}
