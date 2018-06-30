package com.shaukan.gabriel.tinderclone.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shaukan.gabriel.tinderclone.R;

//creating ChatViewHolder class
public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mMessage;
    public CardView mBubble;
    public LinearLayout mContainer;
    public LinearLayout mMasterContainer;

    public ChatViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mBubble = itemView.findViewById(R.id.bubble);
        mMessage = itemView.findViewById(R.id.message);
        mContainer = itemView.findViewById(R.id.container);
        mMasterContainer = itemView.findViewById(R.id.masterContainer);
    }

    @Override
    public void onClick(View view) {
    }
}
