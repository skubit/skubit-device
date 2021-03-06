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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class PurchasesFragment extends Fragment {

	public static PurchasesFragment newInstance() {
		return new PurchasesFragment();
	}

	private ListView listView;

	public void displayPurchases(Context context, ListView view,
			ArrayList<String> purchases) throws JSONException {

		List<Map<String, String>> items = new ArrayList<Map<String, String>>();

		for (String detail : purchases) {
			Map<String, String> map = new HashMap<String, String>();
			JSONObject jo = new JSONObject(detail);
			map.put("title", jo.toString());
			items.add(map);
		}
		String[] from = new String[] { "title" };
		int[] to = new int[] { android.R.id.text1 };

		SimpleAdapter adapter = new SimpleAdapter(context, items,
				android.R.layout.simple_list_item_1, from, to);
		view.setAdapter(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		listView = new ListView(getActivity());
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

			}

		});

		return listView;
	}

	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) getActivity()).fetchPurchases(listView);
	}
}
