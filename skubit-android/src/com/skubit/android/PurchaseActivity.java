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

package com.skubit.android;

import java.text.MessageFormat;

import com.skubit.android.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.skubit.android.billing.BillingResponseCodes;
import com.skubit.android.billing.PurchaseData;
import com.skubit.android.currencies.Bitcoin;
import com.skubit.android.currencies.Satoshi;
import com.skubit.android.services.InventoryService;
import com.skubit.android.services.PurchaseService;
import com.skubit.android.services.rest.InventoryRestService;
import com.skubit.android.services.rest.PurchaseRestService;
import com.skubit.shared.dto.PurchaseDataDto;
import com.skubit.shared.dto.PurchaseDataStatus;
import com.skubit.shared.dto.SkuDetailsDto;

public final class PurchaseActivity extends Activity implements PurchaseView {

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static Intent newIntent(Account googleAccount, PurchaseData data) {
        Intent intent = new Intent(data.sku);

        Parcel parcel = Parcel.obtain();
        data.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        intent.putExtra("PurchaseActivity.account", googleAccount);
        intent.putExtra("PurchaseActivity.purchaseData", parcel.marshall());
        if(Constants.IS_PRODUCTION) {
            intent.setClassName("com.skubit.android",
                    PurchaseActivity.class.getName());
        } else {
            intent.setClassName("net.skubit.android",
                    PurchaseActivity.class.getName());            
        }

        return intent;
    }

    private Account mAccount;

    private InventoryRestService mInventoryService;

    private View mLoading;

    private View mMain;

    private TextView mPrice;

    private Button mPurchaseBtn;

    private PurchaseData mPurchaseData;

    private TextView mTitle;
    
    private TextView mEmail;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(BillingResponseCodes.RESULT_USER_CANCELED,
                new Intent().putExtra("RESPONSE_CODE", BillingResponseCodes.RESULT_USER_CANCELED));
    }

    private void getSkuDetails() {
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
                            String price = new Bitcoin(new Satoshi(skuDetailsDto.getSatoshi())).getDisplay();
                            mPrice.setText(price + "BTC");
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
    public void hideLoading() {
        mMain.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.setContentView(R.layout.purchase_activity_frame);
        this.mLoading = this.findViewById(R.id.progress_bar);
        this.mMain = this.findViewById(R.id.purchase_activity);
        
        new FontManager(this);
        
        mAccount = (Account) this.getIntent().getParcelableExtra(
                "PurchaseActivity.account");
        if(mAccount == null) {
            showMessage("User account has not yet been configured");
            //TODO: Button should take user to app
            return;
        }

        mInventoryService = new InventoryService(mAccount, this)
                .getRestService();

        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setTypeface(FontManager.LITE);
        
        mPrice = (TextView) findViewById(R.id.price);
        mPrice.setTypeface(FontManager.LITE);
        
        mEmail = (TextView) findViewById(R.id.email);
        if(mAccount != null) {
            mEmail.setText(mAccount.name);
            mEmail.setTypeface(FontManager.LITE);
        }
                
        mPurchaseBtn = (Button) this.findViewById(R.id.purchase_btn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAccount != null) {
            showLoading();
            byte[] byteArrayExtra = getIntent().getByteArrayExtra(
                    "PurchaseActivity.purchaseData");
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(byteArrayExtra, 0, byteArrayExtra.length);
            parcel.setDataPosition(0);
            mPurchaseData = PurchaseData.CREATOR.createFromParcel(parcel);
            parcel.recycle();
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    getSkuDetails();
                }
            });
            t.start();            
        }
    }

    private void putPurchaseData(PurchaseDataDto request) {
        PurchaseRestService service = new PurchaseService(mAccount, this)
                .getRestService();

        service.postPurchaseData(mPurchaseData.packageName, mPurchaseData.sku,
                request, new Callback<PurchaseDataDto>() {

                    @Override
                    public void failure(RetrofitError error) {
                        error.printStackTrace();
                        showMessage("Unable to find the SKU you requested");//seems to throw this with valid purchase
                    }

                    @Override
                    public void success(final PurchaseDataDto purchaseDataDto,
                            Response response) {
                        showMessage("Purchase successful");
                        setResult(0, new Intent().putExtra("RESPONSE_CODE", 0));
                    }

                });
    }

    private void showLoading() {
        mMain.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }

    private void showMessage(String message) {
        mLoading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.INVISIBLE);
        TextView messageView = (TextView) findViewById(R.id.purchase_text);
        messageView.setTypeface(FontManager.LITE);
        messageView.setText(message);
    }
}
