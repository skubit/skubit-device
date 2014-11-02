
package com.skubit.android.qr;

import java.math.BigDecimal;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import net.skubit.android.R;
import android.accounts.Account;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.skubit.android.AccountSettings;
import com.skubit.android.BitcoinUri;
import com.skubit.android.Utils;
import com.skubit.android.services.AccountsService;
import com.skubit.android.services.rest.AccountsRestService;
import com.skubit.android.zxing.QRCodeEncoder;
import com.skubit.shared.dto.BitcoinAddressDto;

public class QrCodeActivity extends Activity {

    private AccountSettings mAccountSettings;

    private TextView mAddress;

    private String mAddressValue;

    private String mAmountValue;

    private ImageButton mCopyButton;

    private View mLoading;

    private View mMain;

    private String mNoteValue;

    private ImageView mQrCode;

    private ImageButton mShareButton;

    private AccountsRestService mAccountsService;

    private ImageButton mNewAddressButton;

    private TextView mAmount;

    private void copyToClipboard(String address) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(
                Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Bitcoin Address", address);
        clipboard.setPrimaryClip(clip);
    }

    // bitcoin:<address>[?[amount=<size>][&][label=<label>][&][message=<message>]]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.qrcode_activity_frame);
        mAccountSettings = AccountSettings.get(this);
        mQrCode = (ImageView) findViewById(R.id.qr);
        mAddress = (TextView) this.findViewById(R.id.bitcoin_address);
        mAmount = (TextView) this.findViewById(R.id.bitcoin_amount);
        
        String account = mAccountSettings.retrieveGoogleAccount();
        if (TextUtils.isEmpty(account)) {
            return;
        }

        mAccountsService = new AccountsService(new Account(account, "com.google"), this)
                .getRestService();
        
        this.mLoading = this.findViewById(R.id.progress_bar);
        this.mMain = this.findViewById(R.id.qrcode_activity);
        
        mNoteValue = this.getIntent().getStringExtra("NOTE");
        if (TextUtils.isEmpty(mNoteValue)) {
            mNoteValue = "My Bitcoin address at Skubit";
        }

        mAmountValue = this.getIntent().getStringExtra("AMOUNT");
        if(!TextUtils.isEmpty(mAmountValue)) {
            mAmount.setText(mAmountValue + " BTC");
        }
        mAddressValue = this.getIntent().getStringExtra("ADDRESS");
        if (TextUtils.isEmpty(mAddressValue)) {
            mAddressValue = mAccountSettings.retrieveBitcoinAddress();
            if(TextUtils.isEmpty(mAddressValue)) {
                showLoading();
                loadBitcoinAddress();
            }
            
        } else {
            mAddress.setText(mAddressValue);
        }

        showQrCode();

        mCopyButton = (ImageButton) findViewById(R.id.copyButton);
        mCopyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                BitcoinUri uri = new BitcoinUri();
                uri.address = mAddressValue;
                uri.message = mNoteValue;
                copyToClipboard(uri.toString());
                Toast.makeText(getBaseContext(), "Copied to clipboard",
                        Toast.LENGTH_SHORT).show();
            }

        });
        mShareButton = (ImageButton) findViewById(R.id.shareButton);
        mShareButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                BitcoinUri uri = new BitcoinUri();
                uri.address = mAddressValue;
                startActivity(Intent.createChooser(
                        Utils.createShareIntent(uri, mNoteValue),
                        "Share Bitcoin address to..."));
            }

        });
        
        mNewAddressButton = (ImageButton) findViewById(R.id.newAddressButton);
        mNewAddressButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showLoading();
                mAccountsService.newBitcoinAddress(new Callback<BitcoinAddressDto>() {

                    @Override
                    public void failure(RetrofitError error) {
                       error.printStackTrace();
                       finish();
                    }

                    @Override
                    public void success(BitcoinAddressDto bitcoinAddress, Response response) {
                        mAddressValue = bitcoinAddress.getAddress();
                        mAccountSettings.saveBitcoinAddress(mAddressValue);
                        
                        showMain();
                        showQrCode();             
                        
                    }
                    
                });
            }

        });
        
    }
    
    public void showLoading() {
        mMain.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }
    
    public void showMain() {
        mLoading.setVisibility(View.INVISIBLE);
        mMain.setVisibility(View.VISIBLE);
    }

    private void loadBitcoinAddress() {
        mAccountsService.getCurrentAddress(new Callback<BitcoinAddressDto>() {

            @Override
            public void failure(RetrofitError error) {
               error.printStackTrace();
                finish();
            }

            @Override
            public void success(BitcoinAddressDto bitcoinAddress, Response response) {
                mAddressValue = bitcoinAddress.getAddress();
                mAccountSettings.saveBitcoinAddress(mAddressValue);
                
                showMain();
                showQrCode();               
            }
            
        });
    }
    
    private void showQrCode() {
        if (!TextUtils.isEmpty(mAddressValue)) {
            mAddress.setText(mAddressValue);
            int dimension = (int) (250 * getResources().getDisplayMetrics().density);
            try {
                BitcoinUri uri = new BitcoinUri();
                uri.address = mAddressValue;
                uri.message = mNoteValue;
                if (!TextUtils.isEmpty(mAmountValue)) {
                    uri.setAmount(new BigDecimal(mAmountValue));
                }

                Bitmap bitmap = QRCodeEncoder.encodeAsBitmap(uri.toString(), dimension);
                mQrCode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }
}
