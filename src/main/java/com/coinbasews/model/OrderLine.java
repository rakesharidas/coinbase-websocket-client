package com.coinbasews.model;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class OrderLine {
    enum OrderType{
        BID,ASK;
    }
    Double price;
    Double quantity;
    OrderType orderType;
}
