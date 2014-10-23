/**
 * Copyright 2014 Skubit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.skubit.android.transactions;

import com.skubit.android.R;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skubit.android.AccountSettings;
import com.skubit.android.FontManager;
import com.skubit.android.services.TransactionService;
import com.skubit.shared.dto.TransactionDto;
import com.skubit.shared.dto.TransactionsListDto;

public class TransactionsFragment extends Fragment {

    public static TransactionsFragment newInstance() {
        return new TransactionsFragment();
    }

    private BroadcastReceiver mAccountChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.clear();
            refreshBalance();
            getTransactions();
        }
    };

    private AccountSettings mAccountSettings;

    private TransactionsAdapter mAdapter;

    private TextView mBalance;

    private TransactionService mTransactionService;

    public void getTransactions() {
        if(mTransactionService == null) {
            return;
        }
        mTransactionService.getRestService().getTransactions(500, 0, null,
                new Callback<TransactionsListDto>() {

                    @Override
                    public void failure(RetrofitError error) {
                        error.printStackTrace();
                    }

                    @Override
                    public void success(TransactionsListDto dto, Response response) {
                        mAdapter.clear();
                        mAdapter.addTransactions(dto.getItems());
                    }

                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mAccountSettings = AccountSettings.get(getActivity());
        mAdapter = new TransactionsAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wallet_transactions_fragment, null);
        TextView balanceLabel = (TextView) view.findViewById(R.id.wallet_balance_label);
        balanceLabel.setTypeface(FontManager.CONDENSED_REGULAR);

        mBalance = (TextView) view.findViewById(R.id.wallet_balance);

        ListView list = (ListView) view.findViewById(R.id.list);
        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View arg1, int position,
                    long arg3) {
                TransactionDto transactionDto = (TransactionDto) adapter
                        .getItemAtPosition(position);
                ObjectMapper mapper = new ObjectMapper();
                String value;
                try {
                    value = mapper.writeValueAsString(transactionDto);
                    startActivity(TransactionDetailsActivity.newIntent(value, getActivity()
                            .getPackageName()));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
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

        String account = mAccountSettings.retrieveGoogleAccount();
        if (!TextUtils.isEmpty(account)) {
            mTransactionService = new TransactionService(new Account(account,
                    "com.google"), getActivity());
            refreshBalance();
            getTransactions();
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mAccountChangeReceiver,
                new IntentFilter("account"));
    }

    public void refreshBalance() {
        String account = mAccountSettings.retrieveGoogleAccount();
        if (TextUtils.isEmpty(account)) {
            this.mBalance.setText("Loading");
            return;
        }

        mTransactionService.getRestService().getBalance(new Callback<String>() {

            @Override
            public void failure(RetrofitError arg0) {
                Toast.makeText(getActivity(), "Failed to retrieve balance",
                        Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void success(String balance, Response response) {
                mBalance.setText(mBalance + " BTC");
                mBalance.setTextColor(getActivity().getResources().getColor(
                        R.color.wallet_balance_color));

                mBalance.setText(balance);
            }

        });
    }
}
