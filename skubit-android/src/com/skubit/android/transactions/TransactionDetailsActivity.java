package com.skubit.android.transactions;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skubit.android.FontManager;
import com.skubit.shared.dto.TransactionDto;
import com.skubit.shared.dto.TransactionState;
import com.skubit.shared.dto.TransactionType;

import com.skubit.android.R;
import android.app.Activity;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.widget.TextView;

public class TransactionDetailsActivity extends Activity {

    private DecimalFormat mFormat;

    public static Intent newIntent(String data, String packageName) {
        Intent intent = new Intent();
        intent.setClassName(packageName, TransactionDetailsActivity.class.getName());
        intent.putExtra("transaction", data);
        return intent;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.wallet_transactiondetails);
        setTitle("Transaction Details");
      
        mFormat = new DecimalFormat("0.00000000");

        //TextView from = (TextView) this.findViewById(R.id.transactiondetails_from);
       // TextView to = (TextView) this.findViewById(R.id.transactiondetails_to);
        TextView notes = (TextView) this.findViewById(R.id.transactiondetails_notes);
        TextView amount = (TextView) this.findViewById(R.id.transactiondetails_amount);
        TextView status = (TextView) this.findViewById(R.id.transactiondetails_status);
        TextView date = (TextView) this.findViewById(R.id.transactiondetails_date);
        TextView label = (TextView) this.findViewById(R.id.transactiondetails_label_amount);
        
        ObjectMapper mapper = new ObjectMapper();
        TransactionDto transactionDto = null;
        try {
            transactionDto = mapper.readValue(getIntent().getStringExtra("transaction"), TransactionDto.class);            
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(transactionDto == null) {
            return;
        }
        
        TransactionType type = transactionDto.getTransactionType();
        if(type.equals(TransactionType.PURCHASE)) {
            label.setText("Purchased Amount");
        } else if(type.equals(TransactionType.DEPOSIT)) {
            label.setText("Amount Deposited");
        } else if(type.equals(TransactionType.WITHDRAWL)) {
            label.setText("Amount Withdrew");
        }  else if(type.equals(TransactionType.REFUND)) {
            label.setText("Amount Refunded");
        }
        
        System.out.println(transactionDto);
        formatStatus(status, transactionDto.getTransactionState());
        formatBalance(amount, transactionDto.getAmount());
       // amount.setText(mFormat.format(Double.parseDouble(transactionDto.getAmount())));
        
        notes.setText(transactionDto.getNote());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy, 'at' hh:mma zzz");
        try {       
            date.setText(dateFormat.format(transactionDto.getCreatedDate()));
        } catch (ParseException e) {
            e.printStackTrace();
            date.setText(null);
        }
        
        /*
        try {

            JSONObject amountObj = transaction.getJSONObject("amount");
            amount.setText(mFormat.format(Double.parseDouble(amountObj.getString("amount"))));

            status.setText(transaction.getString("status"));

            JSONObject sender = transaction.getJSONObject("sender");
            from.setText(sender.getString("name") + " (" + sender.getString("email") + ")");

            JSONObject recipient = transaction.getJSONObject("recipient");
            to.setText(recipient.getString("name") + " (" + recipient.getString("email") + ")");

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy, 'at' hh:mma zzz");
            try {
                String createdAt = transaction.optString("created_at");
                date.setText(dateFormat.format(ISO8601.toCalendar(createdAt).getTime()));
            } catch (ParseException e) {
                date.setText(null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
    }
    
    private void formatStatus(TextView view, TransactionState status) {
        view.setText(status.name());
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
        view.setTextColor(getResources().getColor(color));
    }
    
    private static final String formatCurrencyAmount(BigDecimal balanceNumber) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(8);
        numberFormat.setMinimumFractionDigits(4);

        if (balanceNumber.compareTo(BigDecimal.ZERO) == -1) {
            balanceNumber = balanceNumber.multiply(new BigDecimal(-1));
        }

        return numberFormat.format(balanceNumber);
    }

}

