
package com.skubit.android;

import java.text.MessageFormat;

import net.skubit.android.R;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.skubit.android.billing.PurchaseData;
import com.skubit.android.currencies.Bitcoin;
import com.skubit.android.currencies.Satoshi;
import com.skubit.android.services.PurchaseService;
import com.skubit.android.services.rest.PurchaseRestService;
import com.skubit.shared.dto.ErrorMessage;
import com.skubit.shared.dto.PurchaseDataDto;
import com.skubit.shared.dto.PurchasingType;
import com.skubit.shared.dto.SkuDetailsDto;

public class DonationActivity extends BasePurchaseActivity {

    public static Intent newIntent(Account googleAccount, PurchaseData data, String packageName) {
        Intent intent = new Intent(data.sku);

        Parcel parcel = Parcel.obtain();
        data.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        intent.putExtra("PurchaseActivity.account", googleAccount);
        intent.putExtra("PurchaseActivity.purchaseData", parcel.marshall());
        intent.setClassName(packageName,
                DonationActivity.class.getName());
        return intent;
    }

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
                        final PurchasingType type = skuDetailsDto.getType();
                        if (type.equals(PurchasingType.contribution)) {
                            mPurchaseLabel.setText("Enter Contribution Amount (BTC)");
                            mPurchaseBtn.setText("DONATE");
                        } else if(type.equals(PurchasingType.donation)){
                            mPurchaseLabel.setText("Enter Donation Amount (BTC)");
                            mPurchaseBtn.setText("DONATE");
                        } else if(type.equals(PurchasingType.gift)){
                            mPurchaseLabel.setText("Enter Gift Amount (BTC)");
                            mPurchaseBtn.setText("GIFT");
                        }
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
                                        request.setPurchasingType(type);
                                        putPurchaseData(request);
                                        // send developer payload
                                    }

                                });
                                t.start();
                            }
                        });

                        String message = MessageFormat.format("{0} ({1})",
                                skuDetailsDto.getDescription(), skuDetailsDto.getTitle());
                        mTitle.setText(message);
                        setResult(0, new Intent().putExtra("RESPONSE_CODE", 0));
                    }

                });
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {    
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.donation_activity_frame);
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void putPurchaseData(PurchaseDataDto request) {
        PurchaseRestService service = new PurchaseService(mAccount, this)
                .getRestService();
        if (!isNumeric(mAmount.getText().toString())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DonationActivity.this, "Enter a valid amount",
                            Toast.LENGTH_SHORT).show();
                    mAmount.setText("");
                    hideMessage();
                }
            });

            return;
        }
        Satoshi satoshi = new Satoshi(new Bitcoin(mAmount.getText().toString()));

        request.setSatoshi(satoshi.getValueAsLong());
        service.postPurchaseData(mPurchaseData.packageName, mPurchaseData.sku,
                request, new Callback<PurchaseDataDto>() {

                    @Override
                    public void failure(RetrofitError error) {
                        error.printStackTrace();
                        String json = new String(((TypedByteArray) error.getResponse().getBody())
                                .getBytes());
                        ErrorMessage message = new Gson().fromJson(json, ErrorMessage.class);

                        // ErrorMessage message = ;//(ErrorMessage)
                        // error.getBodyAs(ErrorMessage.class);
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
                        showMessage("Donation successful. Thank you.");
                        setResult(0, new Intent().putExtra("RESPONSE_CODE", 0));
                    }

                });
    }

}
