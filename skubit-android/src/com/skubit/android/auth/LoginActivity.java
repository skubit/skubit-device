
package com.skubit.android.auth;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.skubit.android.AccountSettings;
import com.skubit.android.Constants;
import net.skubit.android.R;
import com.skubit.android.provider.accounts.AccountsColumns;
import com.skubit.android.provider.accounts.AccountsContentValues;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.login_webview);
    }

    
    @Override
    protected void onStart() {
        super.onStart();
        WebView webView = (WebView) this.findViewById(R.id.loginWebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("bitid://")) {
                    if(isAuthenticatorInstalled()) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(i);
                        return true;  
                    } else {
                        openAuthenticatorPage();
                        return true; 
                    }
                } else if(url.startsWith("https://play.google.com/store/apps")) {
                    openAuthenticatorPage();
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("/#/mobileLoginResult")) {// success page
                    String userId = url.split("=")[1];

                    String cookie = Constants.IS_PRODUCTION ? CookieManager.getInstance()
                            .getCookie(
                                    "https://catalog.skubit.com") : CookieManager.getInstance()
                            .getCookie(
                                    "https://catalog.skubit.net");

                    AccountsContentValues kcv = new AccountsContentValues();
                    kcv.putBitid(userId);
                    kcv.putCookie(cookie);
                    kcv.putDate(new Date().getTime());

                    getContentResolver().delete(AccountsColumns.CONTENT_URI,
                            AccountsColumns.BITID + "=?",
                            new String[] {
                                userId
                            });

                    getContentResolver().insert(AccountsColumns.CONTENT_URI, kcv.values());
                    AccountSettings.get(LoginActivity.this).saveCookie(cookie);
                    AccountSettings.get(LoginActivity.this).saveBitIdAccount(userId);
                    finish();
                }
            }

        });
        webView.setWebChromeClient(new WebChromeClient() {

        });
        CookieManager.getInstance().removeAllCookie();
        CookieManager.getInstance().setAcceptCookie(true);

        if (Constants.IS_PRODUCTION) {
            webView.loadUrl("https://catalog.skubit.com/#/mobileLogin");
        } else {
            webView.loadUrl("https://catalog.skubit.net/#/mobileLogin");
        }
    }

    private void openAuthenticatorPage() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.skubit.bitid")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.skubit.bitid")));
        }
    }
    private boolean isAuthenticatorInstalled() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.skubit.bitid", PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return false;
        }
        return true;
    }

}
