
package com.skubit.android.qr;

import com.skubit.android.R;
import android.accounts.AccountManager;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.skubit.android.AccountSettings;
import com.skubit.android.zxing.QRCodeEncoder;

public class QrCodeActivity extends Activity {

    private AccountManager mAccountManager;

    private AccountSettings mAccountSettings;

    private TextView mAddress;

    private ImageButton mCopyButton;

    private ImageView mQrCode;

    private ImageButton mShareButton;

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(
                Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Bitcoin Address", text);
        clipboard.setPrimaryClip(clip);
    }

    private void fillBitcoinAddressField() {
        String address = mAccountSettings.retrieveBitcoinAddress();
        mAddress.setText((TextUtils.isEmpty(address) ? "Not added yet" : address));
        // bitcoin:<address>[?[amount=<size>][&][label=<label>][&][message=<message>]]
        if(!TextUtils.isEmpty(address)) {
            int dimension = (int) (250 * getResources().getDisplayMetrics().density);
            try {
                Bitmap bitmap = QRCodeEncoder.encodeAsBitmap("bitcoin:" + address, dimension);
                mQrCode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }            
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.qrcode_activity);

        mAccountManager = AccountManager.get(this);
        mAccountSettings = AccountSettings.get(this);

        mQrCode = (ImageView) findViewById(R.id.qr);
        mAddress = (TextView) this.findViewById(R.id.bitcoin_address);
        mCopyButton = (ImageButton) findViewById(R.id.copyButton);
        mCopyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                copyToClipboard(mAddress.getText().toString());
                Toast.makeText(getBaseContext(), "Copied to clipboard",
                        Toast.LENGTH_SHORT).show();
            }

        });
        mShareButton = (ImageButton) findViewById(R.id.shareButton);
        mShareButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                share(mAddress.getText().toString());
            }

        });
        fillBitcoinAddressField();

    }

    private void share(String address) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "My Bitcoin address at Skubit");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "bitcoin:" + address);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share Bitcoin address to..."));
    }

}
