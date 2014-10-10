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
package com.skubit.android.example;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONException;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ListView;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.skubit.android.billing.IBillingService;

public class MainActivity extends FragmentActivity {

	public class TabAdapter extends FragmentPagerAdapter {

		private final String[] titles = { "Skus", "Purchases" };

		public TabAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public Fragment getItem(int position) {
			return position == 0 ? SkusFragment.newInstance()
					: PurchasesFragment.newInstance();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}
	}

	private TabAdapter mAdapter;

	private ViewPager mPager;

	private IBillingService mService;

	private ServiceConnection mServiceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = IBillingService.Stub.asInterface(service);
			setTabAdapter(mAdapter);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
		}
	};

	private PagerSlidingTabStrip mTabs;

	public void fetchPurchases(final ListView view) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Bundle purchases = mService.getPurchases(1,
							getApplicationContext().getPackageName(), "inapp",
							null);

					String nextToken = purchases
							.getString("INAPP_CONTINUATION_TOKEN");
					final ArrayList<String> purchaseData = purchases
							.getStringArrayList("INAPP_PURCHASE_DATA_LIST");

					final PurchasesFragment purchasesFragment = (PurchasesFragment) mAdapter
							.getItem(1);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								purchasesFragment.displayPurchases(
										MainActivity.this, view, purchaseData);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});

					// TODO: validate signatures

				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	public void fetchSkus(final ListView view) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				ArrayList<String> ids = new ArrayList<String>();
				///ids.add("445");
				//ids.add("440");
				ids.add("contribA");
				final Bundle skusBundle = new Bundle();
				skusBundle.putStringArrayList("ITEM_ID_LIST", ids);

				try {
					Bundle skuDetails = mService.getSkuDetails(1,
							getApplicationContext().getPackageName(), "donation",
							skusBundle);

					final ArrayList<String> details = skuDetails
							.getStringArrayList("DETAILS_LIST");
					final SkusFragment skusFragment = (SkusFragment) mAdapter
							.getItem(0);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								skusFragment.displaySkus(MainActivity.this,
										view, details);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		});
		t.start();

	}

	public void makePurchase(Map<String, String> map) {
		if (mService == null) {
			return;
		}
		try {
			Bundle buyIntent = mService.getBuyIntent(1, getApplicationContext()
					.getPackageName(), map.get("id"), "donation",
					"developerPayload: " + Math.random());
			PendingIntent pendingIntent = buyIntent.getParcelable("BUY_INTENT");

			startIntentSenderForResult(pendingIntent.getIntentSender(), map
					.get("id").hashCode(), new Intent(), 0, 0, 0);

		} catch (RemoteException | SendIntentException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);
		mAdapter = new TabAdapter(getSupportFragmentManager());

		bindService(new Intent(
				"net.skubit.android.billing.IBillingService.BIND"),
				mServiceConn, Context.BIND_AUTO_CREATE);

		// tabs., skudetails and purchases
		// button on sku list to buy [verify if already bought], consume button
		// after buy
		// button [is billing supported]

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mService != null) {
			this.unbindService(mServiceConn);
		}
	}

	protected void setTabAdapter(FragmentPagerAdapter adapter) {
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(adapter);
		mPager.setCurrentItem(0);

		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mTabs.setViewPager(mPager);
	}

}
