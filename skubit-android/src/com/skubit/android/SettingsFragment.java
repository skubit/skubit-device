
package com.skubit.android;

import net.skubit.android.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.google.android.gms.common.AccountPicker;
import com.skubit.android.billing.BillingServiceBinder;
import com.skubit.android.services.TransactionService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsFragment extends Fragment implements SettingsView {

    private AccountManager mAccountManager;

    private AccountSettings mAccountSettings;

    private TextView mAddress;

    protected BillingServiceBinder mBinder;

    private ImageButton mCopyButton;

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(
                Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Bitcoin Address", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void fillBitcoinAddressField() {
        String address = mAccountSettings.retrieveBitcoinAddress();
        mAddress.setText((TextUtils.isEmpty(address) ? "Not added yet" : address));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mAccountChangeReceiver,
                new IntentFilter("account"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, null);

        mAddress = (TextView) view.findViewById(R.id.address);
        mCopyButton = (ImageButton) view.findViewById(R.id.copyButton);
        mCopyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                copyToClipboard(mAddress.getText().toString());
                Toast.makeText(getActivity(), "Copied to clipboard",
                        Toast.LENGTH_SHORT).show();
            }

        });

        mAccountManager = AccountManager.get(getActivity());
        mAccountSettings = AccountSettings.get(getActivity());

       
        return view;
    }


    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mAccountChangeReceiver);
    }

    @Override
    public void refreshView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fillBitcoinAddressField();
            }
        });
    }

    private BroadcastReceiver mAccountChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshView();
        }
    };

}
