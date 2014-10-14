
package com.skubit.android;

import net.skubit.android.R;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skubit.android.billing.BillingResponseCodes;
import com.skubit.android.billing.PurchaseData;
import com.skubit.android.services.InventoryService;
import com.skubit.android.services.rest.InventoryRestService;
import com.skubit.shared.dto.PurchaseDataDto;

public abstract class BasePurchaseActivity extends Activity implements PurchaseView {

    protected Account mAccount;

    protected TextView mAmount;

    protected TextView mEmail;

    protected InventoryRestService mInventoryService;

    protected View mLoading;

    protected View mMain;

    protected TextView mMessageView;

    protected Button mPurchaseBtn;

    protected PurchaseData mPurchaseData;

    protected TextView mPurchaseLabel;

    protected LinearLayout mPurchaseMessage;

    protected TextView mTitle;

    protected abstract void getSkuDetails();

    @Override
    public void hideLoading() {
        mMain.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
    }

    public void hideMessage() {
        mPurchaseMessage.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.VISIBLE);
    }

    protected boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
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
        this.mLoading = this.findViewById(R.id.progress_bar);
        this.mMain = this.findViewById(R.id.purchase_activity);
        this.mPurchaseLabel = (TextView) findViewById(R.id.purchaseLabel);
        this.mPurchaseMessage = (LinearLayout) this.findViewById(R.id.purchase_message);

        mPurchaseMessage.setVisibility(View.INVISIBLE);
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

        this.mAmount = (EditText) findViewById(R.id.amount);
        this.mMessageView = (TextView) findViewById(R.id.purchase_text);

        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setTypeface(FontManager.LITE);

        mEmail = (TextView) findViewById(R.id.email);
        if (mAccount != null) {
            mEmail.setText(mAccount.name);
            mEmail.setTypeface(FontManager.LITE);
        }

        mPurchaseBtn = (Button) this.findViewById(R.id.purchase_btn);

        Button cancelBtn = (Button) this.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //hideMessage();
                finish();
            }
        });

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

    protected abstract void putPurchaseData(PurchaseDataDto request);

    public void showLoading() {
        mMain.setVisibility(View.INVISIBLE);
        mPurchaseMessage.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }
    
    public void showMessage(String message) {
        mLoading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.INVISIBLE);

        mPurchaseMessage.setVisibility(View.VISIBLE);
        mMessageView.setTypeface(FontManager.LITE);
        mMessageView.setText(message);
    }
}
