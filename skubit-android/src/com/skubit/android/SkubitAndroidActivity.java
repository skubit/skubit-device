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

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.skubit.android.auth.LoginActivity;
import com.skubit.android.billing.BillingServiceBinder;
import com.skubit.android.osl.DisplayLicensesActivity;
import com.skubit.android.provider.accounts.AccountsColumns;
import com.skubit.android.qr.QrCodeActivity;
import com.skubit.android.transactions.TransactionsFragment;

public class SkubitAndroidActivity extends Activity  {

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private static final String sAboutUrl = "https://catalog.skubit.com/#!/about";

    private static final String sHelpUrl = "https://catalog.skubit.com/#!/userinfo";

    private static final String sPrivacyUrl = "https://catalog.skubit.com/#!/privacy";

    private static final String TAG = "PLUS";

    private AccountSettings mAccountSettings;

    private BroadcastReceiver mAccountSignout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String account = intent
                    .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            resignIn(account);
        }
    };

    protected BillingServiceBinder mBinder;

    private int mCurrentPosition;

    private DrawerAdapter mDrawerAdapter;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private LinearLayout mDrawerListFrame;

    private ActionBarDrawerToggle mDrawerToggle;

    private BitIdAccountView mAccountView;

    private ImageLoader mImageLoader;

    private boolean mLoginInProcess;

    private boolean mResolvingError;

    private boolean accountMatchesStoredAccount(String account) {
        return account.equals(mAccountSettings.retrieveBitIdAccount());
    }

    private void doToast(final String message) {
        SkubitAndroidActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SkubitAndroidActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void lockOrientation() {
        Log.d(TAG, "Lock Orientation");
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mResolvingError = savedInstanceState.getBoolean("ResolvingError");
            mLoginInProcess = savedInstanceState.getBoolean("LoginInProcess");
        }
        setContentView(R.layout.main_activity);
        new FontManager(this);

        ViewGroup mActionBarLayout = (ViewGroup) getLayoutInflater()
                .inflate(R.layout.action_bar, null);
        TextView mTitle = (TextView) mActionBarLayout.findViewById(R.id.displayName);
        mTitle.setTypeface(FontManager.LITE);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setCustomView(mActionBarLayout);
        setColorResource(R.color.action_bar_coin_color);

        mAccountSettings = AccountSettings.get(this);
        String cookie = mAccountSettings.retrieveCookie();
        if(TextUtils.isEmpty(cookie)) {
            Intent intent = new Intent();
            intent.setClass(this, LoginActivity.class);
            this.startActivity(intent);
            //TODO: get from db
            //this.signoutOfSkubit();//bad cookie, remove all account info
        }

        mImageLoader = ((SkubitApplication) getApplication())
                .getImageLoader();

        mAccountView = (BitIdAccountView) findViewById(R.id.google_accounts);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerListFrame = (LinearLayout) this.findViewById(R.id.left_drawer_frame);

        mDrawerAdapter = new DrawerAdapter(this, null,
                R.array.drawer_items);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList.setAdapter(mDrawerAdapter);
        mDrawerAdapter.setBoldPosition(0);
        selectItem(0);
        
        mAccountView.initialize(this, mDrawerLayout);     
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int order = item.getOrder();
        if (order == 0) {
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        } else if (order == 1) {
            Intent intent = new Intent();
            intent.setClass(this, LoginActivity.class);
            startActivity(intent);        
        } else if (order == 2) {
            Intent i = new Intent();
            i.setClass(this, RequestMoneyActivity.class);
            startActivity(i);           
        } else if (order == 3) {
            Intent i = new Intent();
            i.setClass(this, SendMoneyActivity.class);
            startActivity(i);
        } else if (order == 4) {
            Intent i = new Intent();
            i.setClass(this, QrCodeActivity.class);
            startActivity(i);
        } else if (order == 5) {
            Intent i = new Intent();
            i.setClass(this, DisplayLicensesActivity.class);
            startActivity(i);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAccountSignout);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        unlockOrientation();
        LocalBroadcastManager.getInstance(this).registerReceiver(mAccountSignout,
                new IntentFilter("signout"));
        mAccountView.setAccountName();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ResolvingError", mResolvingError);
        savedInstanceState.putBoolean("LoginInProcess", mLoginInProcess);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        mAccountSettings.setCurrentIndex(mCurrentPosition);
    }

    private Fragment replaceFragmentFor(String tag, Fragment frag) {
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            fragment = frag;
        }
        getFragmentManager().beginTransaction().replace(R.id.main_container, fragment, tag)
                .commit();
        return fragment;
    }

    private void resignIn(String accountName) {
        //signoutOfSkubit();
    }

    private void selectItem(int position) {
        Intent browserIntent = null;
        if (position < 4) {
            this.mCurrentPosition = position;
        }

        if (position == 0) {
            replaceFragmentFor("transactions", new TransactionsFragment());
        }
        else if (position == 1) {
            replaceFragmentFor("settings", new AccountSettingsFragment());
        }
        else if (position == 2) {
            browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(sAboutUrl));
            startActivity(browserIntent);
        } else if (position == 3) {
            browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(sPrivacyUrl));
            startActivity(browserIntent);
        } else if (position == 4) {
            browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(sHelpUrl));
            startActivity(browserIntent);
        } else if (position == 5) {         
            mLoginInProcess = false;
            signoutOfSkubit();
            position = 0;
            replaceFragmentFor("settings", new AccountSettingsFragment());
        }
        if (position < 2) {
            mDrawerAdapter.setBoldPosition(position);
            mDrawerList.setItemChecked(position, true);
        }

        mDrawerLayout.closeDrawer(mDrawerListFrame);
    }

    protected void setColorResource(int color) {
        String hex = getResources().getString(color);
        getActionBar().setBackgroundDrawable(
                new ColorDrawable(Color.parseColor(hex)));
    }

    private void signoutOfSkubit() {
        final AccountSettings accountSettings = AccountSettings.get(this);

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    accountSettings.saveCookie(null);
                    accountSettings.saveBitIdAccount(null);
                    accountSettings.saveBitcoinAddress(null);
                    getContentResolver().delete(AccountsColumns.CONTENT_URI, null, null);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mAccountView.setAccountName();
                            finish();
                        }
                    });
                                      
                    LocalBroadcastManager.getInstance(SkubitAndroidActivity.this)
                            .sendBroadcast(new Intent("account"));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });
        t.start();
    }

    private void unlockOrientation() {
        Log.d(TAG, "Unlock Orientation");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
}
