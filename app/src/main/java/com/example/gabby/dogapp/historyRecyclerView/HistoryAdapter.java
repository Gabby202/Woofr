package com.example.gabby.dogapp.historyRecyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gabby.dogapp.R;

import java.util.List;

/**
 * Created by Gabby on 3/26/2018.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders> {
    private List<HistoryObject> listItems;
    private Context context; //passed at moment of creation of recycler view

    public HistoryAdapter(List<HistoryObject> itemList, Context context) {
        this.listItems = itemList;
        this.context = context;
    }

    @Override
    public HistoryViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        HistoryViewHolders rcv = new HistoryViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolders holder, int position) {
        holder.walkId.setText(listItems.get(position).getWalkId());
        holder.time.setText(listItems.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return this.listItems.size();
    }
}
