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

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.skubit.android.auth.AuthenticationService;
import com.skubit.android.billing.BillingServiceBinder;
import com.skubit.android.services.TransactionService;

public class SkubitAndroidActivity extends Activity implements MainView {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final String sAboutUrl = "https://catalog.skubit.com/#!/about";

    private static final String sHelpUrl = "https://catalog.skubit.com/#!/userinfo";

    private static final String sPrivacyUrl = "https://catalog.skubit.com/#!/privacy";

    private AccountManager mAccountManager;

    private AccountSettings mAccountSettings;

    private TextView mAddress;

    protected BillingServiceBinder mBinder;

    private ImageButton mCopyButton;

    private TextView mGoogleEmail;

    private TextView mBalance;

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Bitcoin Address", text);
        clipboard.setPrimaryClip(clip);
    }

    private boolean exists(String accountName, String accountType) {
        Account[] accounts = mAccountManager.getAccountsByType(accountType);
        if (accounts != null && accounts.length > 0) {
            for (Account account : accounts) {
                if (account.name.equals(accountName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void fillBitcoinAddressField() {
        String address = mAccountSettings.retrieveBitcoinAddress();
        mAddress.setText((TextUtils.isEmpty(address) ? "Not added yet" : address));
    }

    private void refreshBalance() {
        String account = mAccountSettings.retrieveGoogleAccount();
        if (TextUtils.isEmpty(account)) {
            return;
        }
        TransactionService transactionService = new TransactionService(new Account(account,
                "com.google"),
                this);
        transactionService.getRestService().getBalance(new Callback<String>() {

            @Override
            public void failure(RetrofitError arg0) {
                Toast.makeText(SkubitAndroidActivity.this, "Failed to retrieve balance", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void success(String balance, Response response) {
                mBalance.setText(balance);
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            final Intent data) {

        if (requestCode == 200 && data != null) {
            mGoogleEmail.setText("Setting up...");
            mAddress.setText("Setting up...");
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    final String account = data
                            .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    try {
                        AuthenticationService authService = new AuthenticationService(
                                SkubitAndroidActivity.this);
                        authService.login(account);

                        SkubitAndroidActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mGoogleEmail.setText(account);
                                mAccountSettings.saveGoogleAccount(account);
                                fillBitcoinAddressField();
                                refreshBalance();
                            }
                        });
                    } catch (UserRecoverableAuthException e1) {
                        startActivityForResult(e1.getIntent(),
                                PLAY_SERVICES_RESOLUTION_REQUEST);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        doToast("Login error:" + e1.getMessage());
                    } catch (GoogleAuthException e1) {
                        e1.printStackTrace();
                        doToast("Login error:" + e1.getMessage());
                    }
                }

            });
            t.start();

        } else if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST
                && data != null) {
            mGoogleEmail.setText("Setting up...");
            mAddress.setText("Setting up...");
            final String account = data
                    .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            final String token = data
                    .getStringExtra(AccountManager.KEY_AUTHTOKEN);

            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    AuthenticationService authService = new AuthenticationService(
                            SkubitAndroidActivity.this);
                    try {
                        authService.loginWithCode(account, token);
                    } catch (UserRecoverableAuthException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        doToast(e.getMessage());
                    } catch (GoogleAuthException e) {
                        doToast(e.getMessage());
                    }

                    SkubitAndroidActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mGoogleEmail.setText(account);
                            mAccountSettings.saveGoogleAccount(account);
                            fillBitcoinAddressField();
                            refreshBalance();
                        }
                    });

                }
            });

            t.start();

            SkubitAndroidActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(token)) {
                        mGoogleEmail.setText(account);
                        mAccountSettings.saveGoogleAccount(account);
                        fillBitcoinAddressField();
                    } else {
                        mGoogleEmail.setText("Failed. Try again");
                    }
                }
            });
        } else if (requestCode == 300 && data != null) {

        }
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Constants.IS_PRODUCTION) {
            this.setTitle("Skubit Test");
        }

        mBalance = (TextView) findViewById(R.id.balance);
        mAddress = (TextView) findViewById(R.id.address);
        mCopyButton = (ImageButton) findViewById(R.id.copyButton);
        mCopyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                copyToClipboard(mAddress.getText().toString());
                Toast.makeText(SkubitAndroidActivity.this, "Copied to clipboard",
                        Toast.LENGTH_SHORT).show();
            }

        });

        mGoogleEmail = (TextView) findViewById(R.id.googleEmail);
        mAccountManager = AccountManager.get(this);
        mAccountSettings = AccountSettings.get(this);

        Button addGoogleButton = (Button) findViewById(R.id.addGoogleButton);
        addGoogleButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent accountPickerIntent = AccountPicker
                        .newChooseAccountIntent(null, null,
                                new String[] {
                                    "com.google"
                                }, true, null,
                                null, null, null);
                startActivityForResult(accountPickerIntent, 200);
            }
        });

        ImageButton balanceButton = (ImageButton) findViewById(R.id.balanceButton);
        balanceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                refreshBalance();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(sAboutUrl));
                startActivity(browserIntent);
                return true;
            case R.id.action_help:
                browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(sHelpUrl));
                startActivity(browserIntent);
                return true;
            case R.id.action_privacy:
                browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(sPrivacyUrl));
                startActivity(browserIntent);
                return true;
            case R.id.action_signout:
                new AuthenticationService(this).signout(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
    }

    public void refreshView() {
        refreshBalance();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentGoogleEmail = mAccountSettings.retrieveGoogleAccount();

                if (!TextUtils.isEmpty(currentGoogleEmail)
                        && exists(currentGoogleEmail, "com.google")) {
                    mGoogleEmail.setText(currentGoogleEmail);
                } else {
                    mGoogleEmail.setText("Not added yet");
                }

                fillBitcoinAddressField();
            }
        });
    }

}
