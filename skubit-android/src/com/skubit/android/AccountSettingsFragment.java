
package com.skubit.android;

import net.skubit.android.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.accounts.Account;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skubit.android.billing.BillingServiceBinder;
import com.skubit.android.services.AccountsService;
import com.skubit.android.services.rest.AccountsRestService;
import com.skubit.shared.dto.UserDto;

public class AccountSettingsFragment extends Fragment {

    private BroadcastReceiver mAccountChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshView();
        }
    };
    private AccountSettings mAccountSettings;
    private AccountsRestService mAccountsService;
    protected BillingServiceBinder mBinder;
    private EditText mEmail;
    private EditText mFullName;
    private EditText mPayout;
    private Button mSaveButton;

    private EditText mWebsite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, null);
        mAccountSettings = AccountSettings.get(getActivity());
        String account = mAccountSettings.retrieveGoogleAccount();
        if (TextUtils.isEmpty(account)) {
            showMessage("User account has not yet been configured");
            return view;
        }

        mFullName = (EditText) view.findViewById(R.id.fullName);
        mEmail = (EditText) view.findViewById(R.id.contactEmail);
        mWebsite = (EditText) view.findViewById(R.id.website);
        mPayout = (EditText) view.findViewById(R.id.payout);

        mAccountsService = new AccountsService(new Account(account, "com.google"), getActivity())
                .getRestService();

        mSaveButton = (Button) view.findViewById(R.id.saveBtn);
        mSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                UserDto userDto = new UserDto();
                userDto.setFullName(mFullName.getText().toString());
                userDto.setContactWebsite(mWebsite.getText().toString());
                userDto.setEmail(mEmail.getText().toString());
                userDto.setPayoutAddress(mPayout.getText().toString());

                mAccountsService.putUserProfile(userDto, new Callback<Void>() {

                    @Override
                    public void failure(RetrofitError error) {

                    }

                    @Override
                    public void success(Void arg0, Response arg1) {

                    }

                });
            }

        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mAccountChangeReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mAccountChangeReceiver,
                new IntentFilter("account"));
    }

    public void refreshView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateContact();
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void updateContact() {
        if (mAccountsService != null) {
            mAccountsService.getUserProfile(new Callback<UserDto>() {

                @Override
                public void failure(RetrofitError error) {
                    error.printStackTrace();
                }

                @Override
                public void success(UserDto userDto, Response arg1) {
                    mFullName.setText(userDto.getFullName());
                    mWebsite.setText(userDto.getContactWebsite());
                    mEmail.setText(userDto.getEmail());
                    mPayout.setText(userDto.getPayoutAddress());
                }

            });
        }
    }

}
