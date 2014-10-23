
package com.skubit.android.people;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.plus.PlusShare;
import com.google.gson.Gson;
import com.skubit.android.AccountSettings;
import com.skubit.android.FontManager;
import com.skubit.android.SkubitApplication;
import com.skubit.android.billing.PurchaseData;
import com.skubit.android.purchases.DonationActivity;
import com.skubit.android.services.TransactionService;
import com.skubit.shared.dto.ErrorMessage;
import com.skubit.shared.dto.TransactionDto;
import com.skubit.shared.dto.UserDto;

import net.skubit.android.R;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TransferMoneyActivity extends Activity {

    public static Intent newIntent(Account googleAccount, String toId, String toName,
            String imageUrl,
            String packageName) {
        Intent intent = new Intent(toId);

        intent.putExtra("TransferMoneyActivity.account", googleAccount);
        intent.putExtra("TransferMoneyActivity.toId", toId);
        intent.putExtra("TransferMoneyActivity.toName", toName);
        if (!TextUtils.isEmpty(imageUrl)) {
            intent.putExtra("TransferMoneyActivity.imageUrl", imageUrl);
        }
        intent.setClassName(packageName,
                TransferMoneyActivity.class.getName());
        return intent;
    }

    private TextView mTitle;
    private View mLoading;
    private View mMain;
    private TextView mPurchaseLabel;
    private LinearLayout mTransferMessage;
    private Account mAccount;
    private EditText mAmount;
    private TextView mMessageView;
    private TextView mEmail;
    private String mToId;
    private String mToName;
    private Button mPurchaseBtn;
    private TransactionService mTransactionService;
    private NetworkImageView mIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.transfer_activity_frame);
        super.onCreate(savedInstanceState);

        this.mLoading = this.findViewById(R.id.progress_bar);
        this.mMain = this.findViewById(R.id.purchase_activity);
        this.mPurchaseLabel = (TextView) findViewById(R.id.purchaseLabel);
        this.mTransferMessage = (LinearLayout) this.findViewById(R.id.transfer_message);

        mTransferMessage.setVisibility(View.INVISIBLE);

        mAccount = (Account) this.getIntent().getParcelableExtra(
                "TransferMoneyActivity.account");
        if (mAccount == null) {
            showMessage("User account has not yet been configured");
            // TODO: Button should take user to app
            return;
        }

        mTransactionService = new TransactionService(mAccount, this);

        this.mAmount = (EditText) findViewById(R.id.amount);
        this.mMessageView = (TextView) findViewById(R.id.purchase_text);

        this.mIcon = (NetworkImageView) findViewById(R.id.icon);

        mToId = (String) getIntent().getStringExtra("TransferMoneyActivity.toId");
        mToName = (String) getIntent().getStringExtra("TransferMoneyActivity.toName");
        
        String imageUrl = getIntent().hasExtra("TransferMoneyActivity.imageUrl") ? (String) getIntent()
                .getStringExtra("TransferMoneyActivity.imageUrl")
                : null;

        if (imageUrl != null) {
            SkubitApplication skubitApplication = (SkubitApplication)
                    getApplication();

            mIcon.setImageUrl(imageUrl, skubitApplication.getImageLoader());
        }

        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setTypeface(FontManager.LITE);
        mTitle.setText("Transfer Bitcoin to " + mToName);

        mEmail = (TextView) findViewById(R.id.email);
        if (mAccount != null) {
            mEmail.setText(mAccount.name);
            mEmail.setTypeface(FontManager.LITE);
        }

        mPurchaseBtn = (Button) this.findViewById(R.id.purchase_btn);

        mPurchaseBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {               
                if (!isNumeric(mAmount.getText().toString())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TransferMoneyActivity.this, "Enter a valid amount",
                                    Toast.LENGTH_SHORT).show();
                            mAmount.setText("");
                            hideMessage();
                        }
                    });

                    return;
                }
                showLoading();
                UserDto toUser = new UserDto();
                toUser.setFullName(mToName);
                toUser.setSubject(mToId);
                
                mTransactionService.getRestService().makeTransfer(mAmount.getText().toString(),
                       toUser, new Callback<TransactionDto>() {

                            @Override
                            public void failure(RetrofitError error) {
                                error.printStackTrace();

                                String json = new String(((TypedByteArray)
                                        error.getResponse().getBody())
                                                .getBytes());
                                ErrorMessage message = new
                                        Gson().fromJson(json, ErrorMessage.class);
                                showMessage(message.getMessage());
                            }

                            @Override
                            public void success(TransactionDto arg0, Response arg1) {
                                showMessage("Bitcoin transfer to " + mToName
                                        + " successful. Thank you.");
                            }
                        });

            }

        });

        Button cancelBtn = (Button) this.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button shareBtn = (Button) this.findViewById(R.id.share_btn);
        shareBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PlusShare.Builder builder = new PlusShare.Builder(TransferMoneyActivity.this);
                builder.addCallToAction("GIFT",
                        Uri.parse("https://catalog.skubit.net"),// TODO - per
                                                                // env
                        null);
                builder.setContentUrl(Uri
                        .parse("https://catalog.skubit.net"));
                builder.setText("I sent you a gift of BTC: " + mAmount.getText().toString());

                try {
                    Intent shareIntent = builder.getIntent();
                    startActivityForResult(shareIntent, 10000);
                } catch (Exception e) {
                    Toast.makeText(TransferMoneyActivity.this,
                            "Unable to share gift info. Make sure you have Google+ app installed",
                            Toast.LENGTH_LONG).show();
                    // Activity not found - google plus not installed
                }
                finish();
            }
        });
    }

    public void showLoading() {
        mMain.setVisibility(View.INVISIBLE);
        mTransferMessage.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }

    public void showMessage(String message) {
        mLoading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.INVISIBLE);

        mTransferMessage.setVisibility(View.VISIBLE);
        mMessageView.setTypeface(FontManager.LITE);
        mMessageView.setText(message);
    }

    public void hideMessage() {
        mTransferMessage.setVisibility(View.INVISIBLE);
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
}
