
package com.skubit.android;

import java.math.BigDecimal;

import com.skubit.android.people.TransferMoneyActivity;
import com.skubit.android.qr.QrCodeActivity;

import com.skubit.android.R;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class RequestMoneyActivity extends Activity {

    private ImageButton mCopyButton;

    private ImageButton mShareButton;

    private ImageButton mNfcButton;

    private ImageButton mQrCodeButton;

    private EditText mAmount;

    private AccountSettings mAccountSettings;

    private TextView mEmail;

    private EditText mNote;

    private void copyToClipboard(BitcoinUri uri) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(
                Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Bitcoin Address", uri.toString());
        clipboard.setPrimaryClip(clip);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_activity_frame);

        mAccountSettings = AccountSettings.get(this);
        final String address = mAccountSettings.retrieveBitcoinAddress();

        final String email = mAccountSettings.retrieveGoogleAccount();
        mEmail = (TextView) this.findViewById(R.id.email);
        mEmail.setText(email);

        mNote = (EditText) this.findViewById(R.id.note);
        mAmount = (EditText) this.findViewById(R.id.amount);

        mCopyButton = (ImageButton) findViewById(R.id.copyButton);
        mCopyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!checkAmountField()) {
                    return;
                }
                BitcoinUri uri = new BitcoinUri();
                uri.address = address;
                uri.amount = new BigDecimal(mAmount.getText().toString());
                uri.message = mNote.getText().toString();

                copyToClipboard(uri);
                Toast.makeText(getBaseContext(), "Copied to clipboard",
                        Toast.LENGTH_SHORT).show();
            }

        });
        mShareButton = (ImageButton) findViewById(R.id.shareButton);
        mShareButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!checkAmountField()) {
                    return;
                }
                BitcoinUri uri = new BitcoinUri();
                uri.address = address;
                uri.amount = new BigDecimal(mAmount.getText().toString());
                uri.message = mNote.getText().toString();

                startActivity(Intent.createChooser(
                        Utils.createShareIntent(uri, "Request for Money"),
                        "Send Money Request to..."));

            }

        });

        mNfcButton = (ImageButton) findViewById(R.id.nfcButton);
        mNfcButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!checkAmountField()) {
                    return;
                }
            }

        });
        mQrCodeButton = (ImageButton) findViewById(R.id.qrcodeButton);
        mQrCodeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!checkAmountField()) {
                    return;
                }
                Intent i = new Intent();
                i.setClass(getBaseContext(), QrCodeActivity.class);
                i.putExtra("AMOUNT", mAmount.getText().toString());
                i.putExtra("NOTE", mNote.getText().toString());
                i.putExtra("ADDRESS", address);

                startActivity(i);
            }

        });
    }

    public boolean checkAmountField() {
        if (!Utils.isNumeric(mAmount.getText().toString())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), "Enter a valid amount",
                            Toast.LENGTH_SHORT).show();
                    mAmount.setText("");
                }
            });

            return false;
        }

        return true;
    }
}
