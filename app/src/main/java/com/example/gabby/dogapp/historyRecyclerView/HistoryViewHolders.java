package com.example.gabby.dogapp.historyRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.gabby.dogapp.HistorySingleActivity;
import com.example.gabby.dogapp.R;



public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView walkId, time;
    public HistoryViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        walkId = (TextView) itemView.findViewById(R.id.walkId);
        time = (TextView) itemView.findViewById(R.id.time);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("walkId", walkId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);

    }
}
