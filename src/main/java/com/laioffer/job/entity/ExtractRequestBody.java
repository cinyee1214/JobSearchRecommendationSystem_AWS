package com.laioffer.job.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ExtractRequestBody {
    @JsonProperty("data") // this could be omitted
    public List<String> data;

    @JsonProperty("max_keywords") // snake case
    public int maxKeywords;  // camel case

    public ExtractRequestBody(List<String> data, int maxKeywords) {
        this.data = data;
        this.maxKeywords = maxKeywords;
    }

}
