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

import net.skubit.android.R;
import com.skubit.android.provider.accounts.AccountsColumns;
import com.skubit.android.provider.accounts.AccountsCursor;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

public class BitIdAccountView extends LinearLayout {

    private static void styleAccountEmail(TextView view, String email) {
        view.setText(email);
        view.setTypeface(FontManager.REGULAR);
    }
    private Activity mActivity;
    private SimpleCursorAdapter mAdapter;
    private Context mContext;
    private View mDivider;
    protected DrawerLayout mDrawerLayout;
    protected ListView mDropdownList;
    private ImageView mExpanderIcon;

    private View mSpinner;

    public BitIdAccountView(Context context) {
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.drawer_account_bitid, this, true);
    }

    public BitIdAccountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.drawer_account_bitid, this, true);
    }

    public BitIdAccountView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.drawer_account_bitid, this, true);
    }

    public void closeList() {
        mDropdownList.setVisibility(View.GONE);
        mExpanderIcon.setImageResource(R.drawable.ic_action_expand);
        mDivider.setVisibility(View.GONE);
    }
    
    private String getCurrentAccount() {
        return AccountSettings.get(this.mContext).retrieveBitIdAccount();
    }

    public void initialize(final Activity activity, DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;

        mDropdownList = (ListView) findViewById(R.id.account_dropdown);
        final Cursor c = activity.getContentResolver()
                .query(AccountsColumns.CONTENT_URI, null, null, null, null);

        mAdapter = new SimpleCursorAdapter(activity,
                R.layout.drawer_account_drop_item, c,
                new String[]{AccountsColumns.BITID}, new int[]{R.id.account_name},
                CursorAdapter.FLAG_AUTO_REQUERY);
       
        final AccountsCursor ac = new AccountsCursor(c);
        
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
                        ac.moveToPosition(position);
           
                        ImageView expander = (ImageView) mSpinner.findViewById(R.id.expander);
                        expander.setImageResource(R.drawable.ic_action_expand);
                        
                        AccountSettings.get(mContext).saveBitIdAccount(ac.getBitid());
                        AccountSettings.get(mContext).saveCookie(ac.getCookie());
                        AccountSettings.get(mContext).saveBitcoinAddress(null);
                        setAccountName();
                        
                     //   Intent data = new Intent("signout");
                     //   data.putExtra(AccountManager.KEY_ACCOUNT_NAME, ac.getBitid());                      
                     //   LocalBroadcastManager.getInstance(mContext).sendBroadcast(data);

                        mDivider.setVisibility(View.GONE);
                        mDropdownList.setVisibility(View.GONE);
                        mDrawerLayout.closeDrawers();
                    }
                });
        setAccountName();
    }
    
    public void setAccountName() {
        String name = getCurrentAccount();
        mAdapter.notifyDataSetChanged();

        TextView tv = (TextView) mSpinner.findViewById(R.id.account_name);
        styleAccountEmail(tv, name);
        
        NetworkImageView icon = (NetworkImageView) mSpinner.findViewById(R.id.icon);
        icon.setDefaultImageResId(R.drawable.ic_action_user);
    }
}
