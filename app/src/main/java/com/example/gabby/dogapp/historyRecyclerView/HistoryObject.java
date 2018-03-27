package com.example.gabby.dogapp.historyRecyclerView;

/**
 * Created by Gabby on 3/26/2018.
 */

public class HistoryObject {
    private String walkId;
    private String time;

    public HistoryObject(String walkId, String time) {
        this.walkId = walkId;
        this.time = time;
    }

    public String getWalkId() {

        return walkId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
