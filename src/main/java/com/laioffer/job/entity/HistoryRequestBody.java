package com.laioffer.job.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

// Create ItemHistory Servlet to Handle Favorite Request
// history --> favorite
public class HistoryRequestBody {
    @JsonProperty("user_id")
    public String userId;

    public Item favorite;

}
