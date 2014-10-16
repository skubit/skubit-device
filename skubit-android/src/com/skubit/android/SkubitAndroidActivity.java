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
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.skubit.android.auth.AuthenticationService;
import com.skubit.android.billing.BillingServiceBinder;

public class SkubitAndroidActivity extends Activity implements
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {

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

    private AccountSettings mAccountSettings;

    protected BillingServiceBinder mBinder;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    private GoogleApiClient mGoogleApiClient;

    private boolean mResolvingError;

    private boolean mLoginInProcess;

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

    private void doToast(final String message) {
        SkubitAndroidActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SkubitAndroidActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
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
                loginToSkubit();
            }
        } else if (requestCode == 200 && data != null) {
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
        loginToSkubit();
        final String account = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Log.d(TAG, "loginToSkubit - account ");
        if (!account.equals(mAccountSettings.retrieveGoogleAccount()) && mLoginInProcess != true) {
            Log.d(TAG, "loginToSkubit - code path");
            this.mLoginInProcess = true;
            Intent data = new Intent();
            data.putExtra(AccountManager.KEY_ACCOUNT_NAME, account);
            onActivityResult(200, 0, data);
        }
    }

    private void loginToSkubit() {
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            Log.d(TAG, "loginToSkubit - connecting ");
            mGoogleApiClient.connect();
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
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope("email"))
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        mAccountSettings = AccountSettings.get(this);

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

        String[] drawerItems = getResources().getStringArray(R.array.drawer_items);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, drawerItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        Fragment fragment = null;
        if (savedInstanceState != null) {
            fragment = getFragmentManager().findFragmentByTag("settings");
        } else {
            fragment = new SettingsFragment();
            getFragmentManager().beginTransaction().add(R.id.main_container, fragment, "settings")
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
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
            if ((!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting())
                    && !mResolvingError) {
                Log.d(TAG, "onStart - connecting: " + mLoginInProcess);
                lockOrientation();
                mGoogleApiClient.connect();
            }
        }
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

    private void unlockOrientation() {
        Log.d(TAG, "Unlock Orientation");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        unlockOrientation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
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

    private void selectItem(int position) {
        Intent browserIntent = null;

        if (position == 0) {
            replaceFragmentFor("settings", new SettingsFragment());
        }
        else if (position == 1) {
            replaceFragmentFor("circles", new CirclesFragment());
        }
        else if (position == 2) {
            replaceFragmentFor("contact", new ContactInfoFragment());
        } else if (position == 3) {
            browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(sAboutUrl));
            startActivity(browserIntent);
        } else if (position == 4) {
            browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(sPrivacyUrl));
            startActivity(browserIntent);
        } else if (position == 5) {
            browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(sHelpUrl));
            startActivity(browserIntent);
        } else if (position == 6) {
            Log.d(TAG, "Logout: " + mGoogleApiClient.isConnected());
            mLoginInProcess = false;
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
            }
            new AuthenticationService(this).signout();
            position = 0;
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private static final String TAG = "PLUS";

    @Override
    public void onResult(LoadPeopleResult arg0) {
     
    }

}
