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
package com.skubit.gift;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.skubit.android.billing.IBillingService;

public class MainActivity extends Activity {
	private static final boolean sIsProduction = false;
	
	private static final String sAppPackageName = (sIsProduction) ? "com.skubit.android"
			: "net.skubit.android";

	private static final String sServiceName = (sIsProduction) ? "com.skubit.android.billing.IBillingService.BIND"
			: "net.skubit.android.billing.IBillingService.BIND";

	private IBillingService mService;

	private ServiceConnection mServiceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = IBillingService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
		}
	};

	public boolean isInstalled() {
		for (ApplicationInfo packageInfo : getPackageManager()
				.getInstalledApplications(0)) {
			if (packageInfo.packageName.equals(sAppPackageName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bindService(new Intent(sServiceName), mServiceConn,
				Context.BIND_AUTO_CREATE);
		Button giftButton = (Button) this.findViewById(R.id.giftButton);
		giftButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isInstalled()) {
					showGiftDialog();
				} else {
					showUpdateDialog();
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mService != null) {
			this.unbindService(mServiceConn);
		}
	}

	private void showGiftDialog() {
		try {
			Bundle buyIntent = mService.getBuyIntent(1, getPackageName(),
					"my_gift", "gift", "my_developer_payload");
			int response = buyIntent.getInt("RESPONSE_CODE");
			if (response != BillingResponseCodes.RESULT_OK) {
				Toast.makeText(MainActivity.this, "Error: Code =  " + response,
						Toast.LENGTH_SHORT).show();
				return;
			}
			PendingIntent pendingIntent = buyIntent.getParcelable("BUY_INTENT");

			startIntentSenderForResult(pendingIntent.getIntentSender(), 1,
					new Intent(), 0, 0, 0);
		} catch (RemoteException | SendIntentException e) {

		}
	}

	private void showUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Update Skubit Billing Services");
		builder.setMessage("This service won't run unless you update");
		builder.setPositiveButton("Update",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						try {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri
									.parse("market://details?id="
											+ sAppPackageName)));
						} catch (ActivityNotFoundException e) {
							startActivity(new Intent(
									Intent.ACTION_VIEW,
									Uri.parse("http://play.google.com/store/apps/details?id="
											+ sAppPackageName)));
						}
					}
				});
		builder.create().show();
	}
}
