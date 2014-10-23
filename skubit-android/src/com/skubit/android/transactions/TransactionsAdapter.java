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
package com.skubit.android.transactions;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import net.skubit.android.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skubit.android.FontManager;
import com.skubit.shared.dto.TransactionDto;
import com.skubit.shared.dto.TransactionState;

public class TransactionsAdapter extends BaseAdapter {

    private Activity mContext;

    private LayoutInflater mInflater;

    private ArrayList<TransactionDto> transactions = new ArrayList<TransactionDto>();

    public TransactionsAdapter(Activity ctx) {
        mContext = ctx;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        View view = mInflater.inflate(R.layout.wallet_transactions_item, null);
        TextView title = (TextView) view.findViewById(R.id.transaction_title);
        title.setTypeface(FontManager.LITE);

        TransactionDto transactionDto = transactions.get(position);

        TextView amount = (TextView) view.findViewById(R.id.transaction_amount);
        TextView status = (TextView) view.findViewById(R.id.transaction_status);

        formatBalance(amount, transactionDto.getAmount());
        formatStatus(status, transactionDto.getTransactionState());

        title.setText(transactionDto.getNote());

        TransactionState state = transactionDto.getTransactionState();
        if (TransactionState.COMMIT.equals(state)) {

        } else if (TransactionState.PENDING.equals(state)) {

        } else if (TransactionState.RESERVE.equals(state)) {

        } else if (TransactionState.CANCEL.equals(state)) {

        }

        return view;
    }

    private void formatStatus(TextView view, TransactionState status) {
        int scolor;
        boolean confirmed = (TransactionState.COMMIT.equals(status));
        if (confirmed) {
            scolor = R.color.transaction_inlinestatus_complete;
        } else {
            scolor = R.color.transaction_inlinestatus_pending;
        }

        view.setText(status.name());
        view.setTextColor(mContext.getResources().getColor(scolor));
        view.setTypeface(FontManager.CONDENSED_REGULAR);
    }

    private void formatBalance(TextView view, String amount) {
        String balanceString = formatCurrencyAmount(new BigDecimal(amount));
        if (balanceString.startsWith("-")) {
            balanceString = balanceString.substring(1);
        }

        int sign = new BigDecimal(amount).compareTo(BigDecimal.ZERO);
        int color = sign == -1 ? R.color.transaction_negative
                : (sign == 0 ? R.color.transaction_neutral : R.color.transaction_positive);

        view.setText(balanceString + " BTC");
        view.setTextColor(mContext.getResources().getColor(color));
    }

    public void addTransactions(ArrayList<TransactionDto> trans) {
        transactions.addAll(trans);
    }

    public void clear() {
        this.transactions.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return transactions.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public TransactionDto getItem(int position) {
        return transactions.get(position);
    }

    private static final String formatCurrencyAmount(BigDecimal balanceNumber) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(4);
        numberFormat.setMinimumFractionDigits(4);

        if (balanceNumber.compareTo(BigDecimal.ZERO) == -1) {
            balanceNumber = balanceNumber.multiply(new BigDecimal(-1));
        }

        return numberFormat.format(balanceNumber);
    }

}
