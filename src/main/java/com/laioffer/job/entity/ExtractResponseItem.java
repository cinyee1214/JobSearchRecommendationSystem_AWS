package com.laioffer.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
// Since we do not need all the properties in the response, so we mark @JsonIgnoreProperties. Otherwise you'll get exceptions.
public class ExtractResponseItem {
    public List<Extraction> extractions;
}

