package com.coinbasews.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class L2Update {
    private String type;
    private String product_id;
    private List<String[]> changes;
    private String time;

    public List<OrderLine> getBids(){
        return  changes.stream()
                .filter(strArr -> strArr[0].equals("buy"))
                .map(strArr-> OrderLine.builder()
                        .orderType(OrderLine.OrderType.BID)
                        .price(Double.valueOf(strArr[1]))
                        .quantity(Double.valueOf(strArr[2]))
                        .build())
                .collect(Collectors.toList());
    }

    public List<OrderLine> getAsks(){
        return  changes.stream()
                .filter(strArr -> strArr[0].equals("sell"))
                .map(strArr-> OrderLine.builder()
                        .orderType(OrderLine.OrderType.ASK)
                        .price(Double.valueOf(strArr[1]))
                        .quantity(Double.valueOf(strArr[2]))
                        .build())
                .collect(Collectors.toList());
    }
}
