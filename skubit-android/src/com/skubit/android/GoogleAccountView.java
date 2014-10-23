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

import com.skubit.android.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class GoogleAccountView extends LinearLayout {

    private static void styleAccountEmail(TextView view, String email) {
        view.setText(email);
        view.setTypeface(FontManager.REGULAR);
    }
    private AccountManager mAccountManager;
    private Activity mActivity;
    private AccountAdapter mAdapter;
    private Context mContext;
    private View mDivider;
    protected DrawerLayout mDrawerLayout;
    protected ListView mDropdownList;
    private ImageView mExpanderIcon;
    private GoogleApiClient mGoogleApiClient;
    private ImageLoader mImageLoader;

    private View mSpinner;

    public GoogleAccountView(Context context) {
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.drawer_account_google, this, true);
    }

    public GoogleAccountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.drawer_account_google, this, true);
    }

    public GoogleAccountView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.drawer_account_google, this, true);
    }

    public void closeList() {
        mDropdownList.setVisibility(View.GONE);
        mExpanderIcon.setImageResource(R.drawable.ic_action_expand);
        mDivider.setVisibility(View.GONE);
    }
    
    private String getCurrentGooglePlusAccount() {
        return Plus.AccountApi.getAccountName(mGoogleApiClient);
    }

    public void initialize(final Activity activity, GoogleApiClient googleApiClient,
            DrawerLayout drawerLayout, ImageLoader imageLoader) {
        mDrawerLayout = drawerLayout;
        mGoogleApiClient = googleApiClient;
        mImageLoader = imageLoader;

        mAccountManager = AccountManager.get(mContext);
        mDropdownList = (ListView) findViewById(R.id.account_dropdown);

        mAdapter = new AccountAdapter(activity, imageLoader);
        mAdapter.addAccounts(mAccountManager.getAccountsByType("com.google"));// TODO

        mSpinner = findViewById(R.id.account_google);// TODO
        mSpinner.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ImageView expander = (ImageView) mSpinner.findViewById(R.id.expander);

                if (!mDropdownList.isShown()) {
                    mDropdownList.setVisibility(View.VISIBLE);
                    mDivider.setVisibility(View.VISIBLE);
                    expander.setImageResource(R.drawable.ic_action_collapse);
                    //TODO: close
                } else {
                    mDropdownList.setVisibility(View.GONE);
                    expander.setImageResource(R.drawable.ic_action_expand);
                    mDivider.setVisibility(View.GONE);
                }

            }
        });

        mExpanderIcon = (ImageView) mSpinner.findViewById(R.id.expander);
        mExpanderIcon.setImageResource(R.drawable.ic_action_expand);
        mDivider = findViewById(R.id.account_divider_bottom);
        mDivider.setVisibility(View.GONE);
        mDropdownList.setAdapter(mAdapter);

        mDropdownList
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                            View view, int position, long arg3) {

                        Account account = (Account) adapterView
                                .getItemAtPosition(position);
                        ImageView expander = (ImageView) mSpinner.findViewById(R.id.expander);
                        expander.setImageResource(R.drawable.ic_action_expand);
                        
                        Intent data = new Intent("signout");
                        data.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
                        
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(data);

                        mDivider.setVisibility(View.GONE);
                        mDropdownList.setVisibility(View.GONE);
                        mDrawerLayout.closeDrawers();
                    }
                });
    }
    
    public void setAccountName() {
        String name = getCurrentGooglePlusAccount();
        mAdapter.clear();
        mAdapter.addAccounts(mAccountManager.getAccountsByType("com.google"));
        mAdapter.removeAccount(name);
        mAdapter.notifyDataSetChanged();

        Log.v("GooglePlus", this.getClass().getName() + ": Setting Google AccountName: " + name);
        TextView tv = (TextView) mSpinner.findViewById(R.id.account_name);
        styleAccountEmail(tv, name);

        TextView nickname = (TextView) mSpinner.findViewById(R.id.account_nickname);
        nickname.setTypeface(FontManager.BOLD);
        
        Person person = Plus.PeopleApi.getCurrentPerson(this.mGoogleApiClient);
        if (person != null) {
            nickname.setVisibility(View.VISIBLE);
            if (person.hasDisplayName()) {
                nickname.setText(person.getDisplayName());
            } else if (person.hasNickname()) {
                nickname.setText(person.getNickname());
            } else if (person.hasName()) {
                nickname.setText(person.getName().getFormatted());
            }

            if (person.hasImage()) {
                NetworkImageView icon = (NetworkImageView) mSpinner.findViewById(R.id.icon);
                if (person.getImage().hasUrl()) {
                    icon.setImageUrl(person.getImage().getUrl(), mImageLoader);
                }
            }
        } else {
            nickname.setVisibility(View.GONE);
        }
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.mGoogleApiClient = googleApiClient;
    }
}
