
package com.skubit.android.people;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import net.skubit.android.R;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.plus.model.people.Person;
import com.skubit.android.services.TransactionService;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PeopleAdapter extends BaseAdapter {

    private ArrayList<Person> people = new ArrayList<Person>();

    private Context mContext;

    private LayoutInflater mInflater;

    private ImageLoader mImageLoader;

    private Account mAccount;

    public PeopleAdapter(Context context, ImageLoader imageLoader, Account myAccount) {
        mContext = context;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageLoader = imageLoader;
        mAccount = myAccount;
    }

    public void addPerson(Person person) {
        people.add(person);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return people.size();
    }

    @Override
    public Person getItem(int position) {
        return people.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        this.people.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.people_list_item, null);
        }
        final Person person = people.get(position);

        TextView displayName = (TextView) convertView.findViewById(R.id.displayName);
        displayName.setText(person.getDisplayName());

        NetworkImageView image = (NetworkImageView) convertView.findViewById(R.id.icon);
        image.setImageUrl(person.getImage().getUrl(), mImageLoader);
        image.setDefaultImageResId(R.drawable.ic_action_user);

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = TransferMoneyActivity.newIntent(mAccount, person.getId(),
                        person.getDisplayName(), person.getImage().getUrl(),
                        mContext.getPackageName());

                mContext.startActivity(intent);
                /*
                 * mTransactionService.getRestService().makeTransfer(".10",
                 * person.getId(), new Callback<Void>() {
                 * @Override public void failure(RetrofitError arg0) {
                 * arg0.printStackTrace(); }
                 * @Override public void success(Void arg0, Response arg1) { //
                 * TODO Auto-generated method stub } });
                 */
            }
        });

        return convertView;
    }

}
