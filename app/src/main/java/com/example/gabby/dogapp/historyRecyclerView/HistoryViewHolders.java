package com.example.gabby.dogapp.historyRecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.gabby.dogapp.R;

/**
 * Created by Gabby on 3/26/2018.
 */

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView walkId;
    public HistoryViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        walkId = (TextView) itemView.findViewById(R.id.walkId);
    }

    @Override
    public void onClick(View v) {

    }
}
