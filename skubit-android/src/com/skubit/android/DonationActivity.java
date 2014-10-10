
package com.skubit.android;

import java.io.IOException;
import java.text.MessageFormat;

import org.json.JSONException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.skubit.android.billing.BillingResponseCodes;
import com.skubit.android.billing.PurchaseData;
import com.skubit.android.currencies.Bitcoin;
import com.skubit.android.currencies.Satoshi;
import com.skubit.android.services.InventoryService;
import com.skubit.android.services.PurchaseService;
import com.skubit.android.services.rest.InventoryRestService;
import com.skubit.android.services.rest.PurchaseRestService;
import com.skubit.shared.dto.ErrorMessage;
import com.skubit.shared.dto.PurchaseDataDto;
import com.skubit.shared.dto.PurchaseDataStatus;
import com.skubit.shared.dto.PurchasingType;
import com.skubit.shared.dto.SkuDetailsDto;

import net.skubit.android.R;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DonationActivity extends Activity implements PurchaseView {

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
        intent.setClassName("net.skubit.android",
                DonationActivity.class.getName());
        return intent;
    }

    private Account mAccount;

    private TextView mDonationLabel;

    private TextView mEmail;

    private InventoryRestService mInventoryService;

    private TextView mAmount;

    private View mLoading;

    private View mMain;

    private Button mPurchaseBtn;

    private PurchaseData mPurchaseData;

    private TextView mTitle;

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
                        final PurchasingType type = skuDetailsDto.getType();
                        if (type.equals(PurchasingType.contribution)) {
                            mDonationLabel.setText("Enter Contribution Amount (BTC)");
                            mPurchaseBtn.setText("DONATE");
                        } else {
                            mDonationLabel.setText("Enter Donation Amount (BTC)");
                            mPurchaseBtn.setText("DONATE");
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
    public void hideLoading() {
        mMain.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(BillingResponseCodes.RESULT_USER_CANCELED,
                new Intent().putExtra("RESPONSE_CODE", BillingResponseCodes.RESULT_USER_CANCELED));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.donation_activity_frame);

        this.mLoading = this.findViewById(R.id.progress_bar);
        this.mMain = this.findViewById(R.id.purchase_activity);
        this.mDonationLabel = (TextView) findViewById(R.id.donationLabel);
        this.mAmount = (EditText) findViewById(R.id.amount);

        new FontManager(this);

        mAccount = (Account) this.getIntent().getParcelableExtra(
                "PurchaseActivity.account");
        if (mAccount == null) {
            showMessage("User account has not yet been configured");
            // TODO: Button should take user to app
            return;
        }

        mInventoryService = new InventoryService(mAccount, this)
                .getRestService();

        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setTypeface(FontManager.LITE);

        mEmail = (TextView) findViewById(R.id.email);
        if (mAccount != null) {
            mEmail.setText(mAccount.name);
            mEmail.setTypeface(FontManager.LITE);
        }

        mPurchaseBtn = (Button) this.findViewById(R.id.purchase_btn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAccount != null) {
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
