package com.dshantanu.smsapp.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dshantanu.smsapp.R;
import com.dshantanu.smsapp.database.SMS;
import com.dshantanu.smsapp.util.Constants;
import com.dshantanu.smsapp.util.Utils;

import java.util.List;

/**
 * Created by Shantanu on 23-08-2016.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {


    private List<SMS> smsList;

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView body, date;
        LinearLayout linearLayout;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            body = (TextView) itemView.findViewById(R.id.tv_li_conversation_body);
            date = (TextView) itemView.findViewById(R.id.tv_li_conversation_date);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.ll_conversation_item_root);
        }
    }

    public ConversationAdapter(List<SMS> smsList) {
        this.smsList = smsList;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View conversationItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.conversation_list_item, parent, false);
        return new ConversationViewHolder(conversationItemView);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        SMS sms = smsList.get(position);
        holder.body.setText(sms.get_msg_body());
        holder.date.setText(Utils.getRealtiveDate(sms.get_datetime()));
        if (sms.get_type() == Constants.SMS_TYPE_RECIEVED) {
            //message received
            holder.body.setBackgroundColor(ContextCompat.getColor(holder.body.getContext(), R.color.recieved_msg));
            holder.linearLayout.setGravity(GravityCompat.START);
        } else {
            //message sent
            holder.body.setBackgroundColor(ContextCompat.getColor(holder.body.getContext(), R.color.sent_msg));
            holder.linearLayout.setGravity(GravityCompat.END);
        }
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

}//end class
