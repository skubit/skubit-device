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

package com.skubit.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.skubit.android.R;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

public class AccountAdapter extends BaseAdapter {

    private List<Account> mAccounts = new ArrayList<Account>();

    private LayoutInflater mInflater;

    public AccountAdapter(Activity ctx, ImageLoader imageLoader) {
        mInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addAccount(Account account) {
        mAccounts.add(account);
    }

    public void addAccounts(Account[] accounts) {
        mAccounts.addAll(Arrays.asList(accounts));
    }

    public void clear() {
        mAccounts.clear();
    }

    @Override
    public int getCount() {
        return mAccounts.size();
    }

    @Override
    public Object getItem(int location) {
        return mAccounts.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        convertView = mInflater.inflate(R.layout.drawer_account_drop_item, null);
        TextView tv = (TextView) convertView.findViewById(R.id.account_name);
        Account account = mAccounts.get(position);
        if (account.type.equals("com.google")) {

        }
        tv.setText(account.name);
        return convertView;
    }

    public void removeAccount(String email) {
        for (Account account : new ArrayList<Account>(mAccounts)) {
            if (account.name.equals(email)) {
                mAccounts.remove(account);
                notifyDataSetChanged();
            }
        }
    }

}
