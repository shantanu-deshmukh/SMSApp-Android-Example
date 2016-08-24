package com.dshantanu.smsapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dshantanu.smsapp.R;
import com.dshantanu.smsapp.database.SMS;
import com.dshantanu.smsapp.ui.ConversationActivity;
import com.dshantanu.smsapp.util.Constants;
import com.dshantanu.smsapp.util.ContactUtil;
import com.dshantanu.smsapp.util.Utils;

import java.util.List;

/**
 * Created by Shantanu on 23-08-2016.
 */
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.MyViewHolder> {

    private List<SMS> smsList;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView addr, body, date;

        public MyViewHolder(View view) {
            super(view);
            addr = (TextView) view.findViewById(R.id.tv_ib_item_addr);
            body = (TextView) view.findViewById(R.id.tv_ib_item_body);
            date = (TextView) view.findViewById(R.id.tv_ib_item_date);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Intent itConversation = new Intent(view.getContext(), ConversationActivity.class);
            itConversation.putExtra(view.getContext().getString(R.string.intent_conversation_id), smsList.get(position).get_threadID());
            view.getContext().startActivity(itConversation);
        }
    }


    public InboxAdapter(List<SMS> smsList) {
        this.smsList = smsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inbox_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SMS sms = smsList.get(position);
        if (sms.get_readState() == Constants.SMS_READ_STATE_UNREAD) {
            //unread sms
            holder.addr.setTextColor(Color.BLACK);
            holder.addr.setText(getContactName(sms.get_address(), holder.addr.getContext()));
        } else {
            holder.addr.setTextColor(Color.GRAY);
            holder.addr.setText(getContactName(sms.get_address(), holder.addr.getContext()));
        }

        holder.body.setText(sms.get_msg_body());
        holder.date.setText(Utils.getRealtiveDate(sms.get_datetime()));
    }


    public String getContactName(String address, Context context) {
        address = address.replace("+", "");
        if (address.matches("[0-9]+")) {
            address = ContactUtil.getContactName(context, address);
        }
        return address;
    }


    @Override
    public int getItemCount() {
        return smsList.size();
    }


}//end class
