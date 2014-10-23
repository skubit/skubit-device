
package com.skubit.android.people;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.skubit.android.AccountSettings;
import com.skubit.android.SkubitAndroidActivity;
import com.skubit.android.SkubitApplication;
import com.skubit.android.services.TransactionService;

import net.skubit.android.R;
import android.accounts.Account;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class PeopleFragment extends Fragment implements ResultCallback<People.LoadPeopleResult> {

    private GoogleApiClient mGoogleApiClient;

    private PeopleAdapter mPeopleAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SkubitApplication skubitApplication = (SkubitApplication) this.getActivity()
                .getApplication();

        AccountSettings mAccountSettings = AccountSettings.get(getActivity());

        String account = mAccountSettings.retrieveGoogleAccount();
   
        mPeopleAdapter = new PeopleAdapter(this.getActivity(), skubitApplication.getImageLoader(),
                new Account(account, "com.google"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.people_fragment, null);
        GridView gc = (GridView) view.findViewById(R.id.gridview);
        gc.setAdapter(mPeopleAdapter);
        return view;
    }

    private static final String TAG = "PLUS";

    @Override
    public void onResult(LoadPeopleResult peopleData) {
        this.mPeopleAdapter.clear();

        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            try {
                int count = personBuffer.getCount();
                for (int i = 0; i < count; i++) {
                    mPeopleAdapter.addPerson(personBuffer.get(i));

                }
                mPeopleAdapter.notifyDataSetChanged();
            } finally {
                // personBuffer.close();
            }
        } else {
            Log.e(TAG, "Error requesting visible circles: " + peopleData.getStatus());
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mAccountChangeReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient = ((SkubitAndroidActivity) this.getActivity()).getGoogleApiClient();
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mAccountChangeReceiver,
                new IntentFilter("account"));
    }

    private BroadcastReceiver mAccountChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(
                    PeopleFragment.this);
        }
    };

}
