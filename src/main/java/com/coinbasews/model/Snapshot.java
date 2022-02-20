package com.coinbasews.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Snapshot {
    private String type;
    private String product_id;
    private List<String[]> asks;
    private List<String[]> bids;

}
