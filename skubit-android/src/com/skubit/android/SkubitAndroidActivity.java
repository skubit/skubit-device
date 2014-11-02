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

import java.io.IOException;

import net.skubit.android.R;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
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
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.skubit.android.auth.AuthenticationService;
import com.skubit.android.billing.BillingServiceBinder;
import com.skubit.android.osl.DisplayLicensesActivity;
import com.skubit.android.people.PeopleFragment;
import com.skubit.android.qr.QrCodeActivity;
import com.skubit.android.transactions.TransactionsFragment;

public class SkubitAndroidActivity extends Activity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final int RC_SIGN_IN = 9001;

    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

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

    private GoogleAccountView mGoogleAccountView;

    private GoogleApiClient mGoogleApiClient;

    private ImageLoader mImageLoader;

    private boolean mLoginInProcess;

    private boolean mResolvingError;

    private boolean accountMatchesStoredAccount(String account) {
        return account.equals(mAccountSettings.retrieveGoogleAccount());
    }

    private boolean checkPlayServices() {
        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (statusCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(statusCode)) {
                GooglePlayServicesUtil.getErrorDialog(statusCode, this,
                        REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
            } else {
                Toast.makeText(this, "This device is not supported.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void connectToGoogleApi() {
        if ((!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting())
                && !mResolvingError) {
            Log.d(TAG, "onStart - connecting: " + mLoginInProcess);
            lockOrientation();
            mGoogleApiClient.connect();
        }
    }

    private void createGooglePlusClient(String accountName) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope("email"))
                .addScope(Plus.SCOPE_PLUS_LOGIN);
        if (!TextUtils.isEmpty(accountName)) {
            builder = builder.setAccountName(accountName);
        }
        mGoogleApiClient = builder.build();
    }

    private void doToast(final String message) {
        SkubitAndroidActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SkubitAndroidActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentGooglePlusAccount() {
        return Plus.AccountApi.getAccountName(mGoogleApiClient);
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
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
    protected void onActivityResult(final int requestCode, int resultCode,
            final Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode);
        if (RC_SIGN_IN == requestCode) {
            Log.d(TAG, "onActivityResult: rc-sign-in");
            mResolvingError = false;
            unlockOrientation();
            if (!mLoginInProcess) {
                signinToSkubit();
            }
        } else if (requestCode == 300 && data != null) {
            String accountName = data
                    .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            signoutOfSkubit();
            Log.d(TAG, "onActivityResult: 300 - " + accountName);
            signoutOfGooglePlus();

            createGooglePlusClient(accountName);
            connectToGoogleApi();

        }
        else if (requestCode == 200 && data != null) {
            Log.d(TAG, "onActivityResult: running login thread");
            Thread loginThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    final String account = data
                            .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d(TAG, "onActivityResult. loginThread - found account" + account);
                    try {
                        AuthenticationService authService = new AuthenticationService(
                                SkubitAndroidActivity.this);
                        authService.login(account);
                        mAccountSettings.saveGoogleAccount(account);
                        LocalBroadcastManager.getInstance(SkubitAndroidActivity.this)
                                .sendBroadcast(new Intent("account"));
                        mLoginInProcess = false;
                        unlockOrientation();
                    } catch (UserRecoverableAuthException e1) {
                        e1.printStackTrace();
                        Log.d(TAG, "onActivityResult. loginThread - User Recoverable");
                        startActivityForResult(e1.getIntent(),
                                PLAY_SERVICES_RESOLUTION_REQUEST);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        doToast("Login error:" + e1.getMessage());
                        mLoginInProcess = false;
                        unlockOrientation();
                    } catch (GoogleAuthException e1) {
                        e1.printStackTrace();
                        doToast("Login error:" + e1.getMessage());
                        mLoginInProcess = false;
                        unlockOrientation();
                    }
                    Log.d(TAG, "onActivityResult. loginThread - finished");

                }

            });
            loginThread.start();

        } else if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST
                && data != null) {
            Log.d(TAG, "onActivityResult. play services request");
            final String account = data
                    .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            final String token = data
                    .getStringExtra(AccountManager.KEY_AUTHTOKEN);
            if (!TextUtils.isEmpty(token)) {
                Log.d(TAG, "onActivityResult. loginWithCodeThread - starting");
                Thread loginWithCodeThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        AuthenticationService authService = new AuthenticationService(
                                SkubitAndroidActivity.this);
                        try {
                            authService.loginWithCode(account, token);
                            mAccountSettings.saveGoogleAccount(account);
                            LocalBroadcastManager.getInstance(SkubitAndroidActivity.this)
                                    .sendBroadcast(new Intent("account"));
                        } catch (UserRecoverableAuthException e) {
                            Log.d(TAG, "onActivityResult. loginWithCodeThread:" + e.getMessage());
                            e.printStackTrace();
                        } catch (IOException e) {
                            Log.d(TAG, "onActivityResult. loginWithCodeThread:" + e.getMessage());
                            doToast(e.getMessage());
                        } catch (GoogleAuthException e) {
                            Log.d(TAG, "onActivityResult. loginWithCodeThread:" + e.getMessage());
                            doToast(e.getMessage());
                        }
                        mLoginInProcess = false;
                        unlockOrientation();
                    }
                });

                loginWithCodeThread.start();
            }
        } else if (requestCode == REQUEST_CODE_RECOVER_PLAY_SERVICES) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Please install Google Play Services.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onConnected(Bundle bunlde) {
        Log.d(TAG, "onConnected");
        mGoogleAccountView.setGoogleApiClient(mGoogleApiClient);
        mGoogleAccountView.setAccountName();

        signinToSkubit();
        final String account = getCurrentGooglePlusAccount();
        Log.d(TAG, "loginToSkubit - account ");
        if (!accountMatchesStoredAccount(account) && mLoginInProcess != true) {
            Log.d(TAG, "loginToSkubit - code path");
            this.mLoginInProcess = true;
            Intent data = new Intent();
            data.putExtra(AccountManager.KEY_ACCOUNT_NAME, account);
            onActivityResult(200, 0, data);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed");
        if (!mResolvingError && result.hasResolution()) {
            try {
                mResolvingError = true;
                Log.d(TAG, "onConnectionFailed - start intent sender");
                this.lockOrientation();
                startIntentSenderForResult(result.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (SendIntentException e) {
                Log.d(TAG, "onConnectionFailed" + e.getMessage());
                mResolvingError = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int result) {

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
            this.signoutOfSkubit();//bad cookie, remove all account info
        }
        createGooglePlusClient(mAccountSettings.retrieveGoogleAccount());

        mImageLoader = ((SkubitApplication) getApplication())
                .getImageLoader();

        mGoogleAccountView = (GoogleAccountView) findViewById(R.id.google_accounts);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mGoogleAccountView.initialize(this, this.mGoogleApiClient, mDrawerLayout,
                mImageLoader);

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

        Fragment fragment = null;
        if (savedInstanceState != null) {
            fragment = getFragmentManager().findFragmentByTag("settings");
        } else {
            fragment = new AccountSettingsFragment();
            getFragmentManager().beginTransaction().add(R.id.main_container, fragment, "settings")
                    .commit();
        }
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
            Intent i = new Intent();
            i.setClass(this, RequestMoneyActivity.class);
            startActivity(i);           
        } else if (order == 2) {
            Intent i = new Intent();
            i.setClass(this, SendMoneyActivity.class);
            startActivity(i);
        } else if (order == 3) {
            Intent i = new Intent();
            i.setClass(this, QrCodeActivity.class);
            startActivity(i);
        } else if (order == 4) {
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
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ResolvingError", mResolvingError);
        savedInstanceState.putBoolean("LoginInProcess", mLoginInProcess);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (checkPlayServices()) {
            connectToGoogleApi();
            selectItem(mAccountSettings.getCurrentIndex());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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
        signoutOfSkubit();
        signoutOfGooglePlus();

        createGooglePlusClient(accountName);
        connectToGoogleApi();
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
            replaceFragmentFor("circles", new PeopleFragment());
        }
        else if (position == 2) {
            replaceFragmentFor("contact", new ContactInfoFragment());
        }
        else if (position == 3) {
            replaceFragmentFor("settings", new AccountSettingsFragment());
        }
        else if (position == 4) {
            PlusShare.Builder builder = new PlusShare.Builder(this);
            builder.addCallToAction("INSTALL_APP",
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()),
                    null);
            builder.setContentUrl(Uri
                    .parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
            builder.setText("Check out the Skubit app: in-app purchases with Bitcoin");

            try {
                Intent shareIntent = builder.getIntent();
                startActivityForResult(shareIntent, 10000);
            } catch (Exception e) {
                Toast.makeText(this,
                        "Unable to share app. Make sure you have Google+ app installed",
                        Toast.LENGTH_LONG).show();
                // Activity not found - google plus not installed
            }
        }
        else if (position == 5) {
            browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(sAboutUrl));
            startActivity(browserIntent);
        } else if (position == 6) {
            browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(sPrivacyUrl));
            startActivity(browserIntent);
        } else if (position == 7) {
            browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(sHelpUrl));
            startActivity(browserIntent);
        } else if (position == 8) {
            Log.d(TAG, "Logout: " + mGoogleApiClient.isConnected());
            mLoginInProcess = false;
            signoutOfGooglePlusAndConnect();
            signoutOfSkubit();
            position = 0;
            replaceFragmentFor("settings", new AccountSettingsFragment());
        }
        if (position < 4) {
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

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    private void signinToSkubit() {
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            Log.d(TAG, "loginToSkubit - connecting ");
            mGoogleApiClient.connect();
        }
    }

    private void signoutOfGooglePlus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
    }

    private void signoutOfGooglePlusAndConnect() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }

    private void signoutOfSkubit() {
        new AuthenticationService(this).signout();
    }

    private void unlockOrientation() {
        Log.d(TAG, "Unlock Orientation");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
}
