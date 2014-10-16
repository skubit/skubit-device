
package com.skubit.android;

import net.skubit.android.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.accounts.Account;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skubit.android.services.AccountsService;
import com.skubit.android.services.rest.AccountsRestService;
import com.skubit.shared.dto.UserDto;

public class ContactInfoFragment extends Fragment {

    private AccountSettings mAccountSettings;

    private AccountsRestService mAccountsService;

    private EditText mAddress1;

    private EditText mAddress2;

    private Button mCancelButton;

    private EditText mCity;

    private EditText mEmployer;

    private EditText mFullName;

    private EditText mOccupation;

    private Button mSaveButton;

    private EditText mState;

    private EditText mZip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_info_fragment, null);
        new FontManager(getActivity());

        mAccountSettings = AccountSettings.get(getActivity());
        String account = mAccountSettings.retrieveGoogleAccount();
        if (TextUtils.isEmpty(account)) {
            showMessage("User account has not yet been configured");
            return view;
        }

        mAddress1 = (EditText) view.findViewById(R.id.address1);
        mAddress2 = (EditText) view.findViewById(R.id.address2);
        mCity = (EditText) view.findViewById(R.id.city);
        mEmployer = (EditText) view.findViewById(R.id.employer);
        mFullName = (EditText) view.findViewById(R.id.fullName);
        mOccupation = (EditText) view.findViewById(R.id.occupation);
        mState = (EditText) view.findViewById(R.id.state);
        mZip = (EditText) view.findViewById(R.id.zip);

        mAccountsService = new AccountsService(new Account(account, "com.google"), getActivity())
                .getRestService();

        mSaveButton = (Button) view.findViewById(R.id.saveBtn);
        mSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                UserDto userDto = new UserDto();
                userDto.setCity(mCity.getText().toString());
                userDto.setEmployer(mEmployer.getText().toString());
                userDto.setFullName(mFullName.getText().toString());
                userDto.setOccuption(mOccupation.getText().toString());
                userDto.setState(mState.getText().toString());
                userDto.setStreetAddress1(mAddress1.getText().toString());
                userDto.setStreetAddress2(mAddress2.getText().toString());
                userDto.setZipCode(mZip.getText().toString());

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
    public void onResume() {
        super.onResume();
        if (mAccountsService != null) {
            mAccountsService.getUserProfile(new Callback<UserDto>() {

                @Override
                public void failure(RetrofitError error) {
                    error.printStackTrace();
                }

                @Override
                public void success(UserDto userDto, Response arg1) {
                    mCity.setText(userDto.getCity());
                    mEmployer.setText(userDto.getEmployer());
                    mFullName.setText(userDto.getFullName());
                    mOccupation.setText(userDto.getOccuption());
                    mState.setText(userDto.getState());
                    mAddress1.setText(userDto.getStreetAddress1());
                    mAddress2.setText(userDto.getStreetAddress2());
                    mZip.setText(userDto.getZipCode());
                }

            });
        }
    }

    private void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

}
