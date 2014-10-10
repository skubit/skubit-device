
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

    private TextView mBalance;

    protected BillingServiceBinder mBinder;

    private ImageButton mCopyButton;

    private TextView mGoogleEmail;

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(
                Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Bitcoin Address", text);
        clipboard.setPrimaryClip(clip);
    }

    private boolean exists(String accountName, String accountType) {
        Account[] accounts = mAccountManager.getAccountsByType(accountType);
        if (accounts != null && accounts.length > 0) {
            for (Account account : accounts) {
                if (account.name.equals(accountName)) {
                    return true;
                }
            }
        }
        return false;
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

        mBalance = (TextView) view.findViewById(R.id.balance);
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

        mGoogleEmail = (TextView) view.findViewById(R.id.googleEmail);
        mAccountManager = AccountManager.get(getActivity());
        mAccountSettings = AccountSettings.get(getActivity());

        Button addGoogleButton = (Button) view.findViewById(R.id.addGoogleButton);
        addGoogleButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent accountPickerIntent = AccountPicker
                        .newChooseAccountIntent(null, null,
                                new String[] {
                                    "com.google"
                                }, true, null,
                                null, null, null);
                getActivity().startActivityForResult(accountPickerIntent, 200);
            }
        });

        ImageButton balanceButton = (ImageButton) view.findViewById(R.id.balanceButton);
        balanceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                refreshBalance();
            }
        });
        return view;
    }

    @Override
    public void refreshBalance() {
        String account = mAccountSettings.retrieveGoogleAccount();
        if (TextUtils.isEmpty(account)) {
            return;
        }
        TransactionService transactionService = new TransactionService(new Account(account,
                "com.google"), getActivity());
        transactionService.getRestService().getBalance(new Callback<String>() {

            @Override
            public void failure(RetrofitError arg0) {
                Toast.makeText(getActivity(), "Failed to retrieve balance",
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void success(String balance, Response response) {
                mBalance.setText(balance);
            }

        });
    }
   
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mAccountChangeReceiver);
    }

    @Override
    public void refreshView() {
        final String currentGoogleEmail = mAccountSettings.retrieveGoogleAccount();
        setEmail(currentGoogleEmail);
        refreshBalance();
        fillBitcoinAddressField();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!TextUtils.isEmpty(currentGoogleEmail)
                        && exists(currentGoogleEmail, "com.google")) {
                    mGoogleEmail.setText(currentGoogleEmail);
                } else {
                    mGoogleEmail.setText("Not added yet");
                }

                fillBitcoinAddressField();
            }
        });
    }

    @Override
    public void setEmail(String email) {
        mGoogleEmail.setText(email);
    }

    private BroadcastReceiver mAccountChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshView();
        }
    };

}
