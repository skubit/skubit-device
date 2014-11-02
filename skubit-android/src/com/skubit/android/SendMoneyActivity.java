
package com.skubit.android;

import net.skubit.android.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coinbase.zxing.client.android.Intents;
import com.google.gson.Gson;
import com.skubit.android.currencies.Bitcoin;
import com.skubit.android.services.TransactionService;
import com.skubit.shared.dto.ErrorMessage;
import com.skubit.shared.dto.TransactionDto;

public class SendMoneyActivity extends Activity {

    private ImageButton mCameraBtn;
    private EditText mSendTo;
    private EditText mAmount;
    private View mLoading;
    private TextView mPurchaseLabel;
    private LinearLayout mSendMessage;
    private TransactionService mTransactionService;
    private TextView mMessageView;
    private Account mAccount;
    private AccountSettings mAccountSettings;
    private View mMain;
    private Button mPurchaseBtn;
    private EditText mNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_activity_frame);

        this.mLoading = this.findViewById(R.id.progress_bar);
        this.mMain = this.findViewById(R.id.send_activity);
        this.mPurchaseLabel = (TextView) findViewById(R.id.purchaseLabel);
        this.mSendMessage = (LinearLayout) this.findViewById(R.id.send_message);

        mNote = (EditText) this.findViewById(R.id.note);

        mSendMessage.setVisibility(View.INVISIBLE);

        mAccountSettings = AccountSettings.get(this);

        mAccount = new Account(mAccountSettings.retrieveGoogleAccount(), "com.google");
        if (mAccount == null) {
            showMessage("User account has not yet been configured");
            // TODO: Button should take user to app
            return;
        }

        mTransactionService = new TransactionService(mAccount, this);

        this.mMessageView = (TextView) findViewById(R.id.purchase_text);

        mSendTo = (EditText) this.findViewById(R.id.sendTo);
        mAmount = (EditText) this.findViewById(R.id.amount);

        mCameraBtn = (ImageButton) this.findViewById(R.id.cameraButton);
        mCameraBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startBarcodeScan();
            }
        });

        if (getIntent().getData() != null && "bitcoin".equals(getIntent().getData().getScheme())) {
            try {
                BitcoinUri bitcoinUri = BitcoinUri.parse(getIntent().getData().toString());
                mSendTo.setText(bitcoinUri.getAddress());
                mAmount.setText(new Bitcoin(bitcoinUri.getAmount().toString()).getDisplay());
                mNote.setText(bitcoinUri.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
            }
        }

        mPurchaseBtn = (Button) this.findViewById(R.id.purchase_btn);

        mPurchaseBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!Utils.isNumeric(mAmount.getText().toString())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), "Enter a valid amount",
                                    Toast.LENGTH_SHORT).show();
                            mAmount.setText("");
                            hideMessage();
                        }
                    });

                    return;
                }
                showLoading();

                mTransactionService.getRestService().sendMoney(mSendTo.getText().toString(),
                        mAmount.getText().toString(), mNote.getText().toString(),
                        new Callback<TransactionDto>() {

                            @Override
                            public void failure(RetrofitError error) {
                                error.printStackTrace();

                                String json = new String(((TypedByteArray)
                                        error.getResponse().getBody())
                                                .getBytes());//could throw NPE on timeout
                                ErrorMessage message = new
                                        Gson().fromJson(json, ErrorMessage.class);
                                showMessage(message.getMessage());
                            }

                            @Override
                            public void success(TransactionDto arg0, Response arg1) {
                                showMessage("Bitcoin transfer to " + mSendTo.getText().toString()
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
                
                BitcoinUri uri = new BitcoinUri();
                uri.address = mSendTo.getText().toString();
                startActivity(Intent.createChooser(
                        Utils.createShareIntent(uri, mNote.getText().toString()),
                        "I sent you money - BTC: " + mAmount.getText().toString()));
                
                finish();
            }
        });

    }

 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        String contents = data.getStringExtra("SCAN_RESULT");
        try {
            BitcoinUri bitcoinUri = BitcoinUri.parse(contents);
            mSendTo.setText(bitcoinUri.getAddress());
            mAmount.setText(new Bitcoin(bitcoinUri.getAmount().toString()).getDisplay());
            mNote.setText(bitcoinUri.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    public void startBarcodeScan() {
        Intent intent = new Intent(this, com.coinbase.zxing.client.android.CaptureActivity.class);
        intent.setAction(Intents.Scan.ACTION);
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    public void showLoading() {
        mMain.setVisibility(View.INVISIBLE);
        mSendMessage.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }

    public void showMessage(String message) {
        mLoading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.INVISIBLE);

        mSendMessage.setVisibility(View.VISIBLE);
        mMessageView.setTypeface(FontManager.LITE);
        mMessageView.setText(message);
    }

    public void hideMessage() {
        mSendMessage.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.VISIBLE);
    }

}
