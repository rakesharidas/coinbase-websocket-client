package com.coinbasews.model;

import lombok.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An OrderBook to keep snapshot of asks and bids.
 */
@Builder
@ToString
@With
@Getter
public class OrderBook {
    String productId;
    Map<Double,Double> bids;
    Map<Double,Double> asks;

    /**
     *  Create a new OrderBook from a Snapshot message.
     * @param snapshot - A snapshot object from the message snapshot type.
     * @return -  An OrderBook from snapshot.
     */
    public static OrderBook from(Snapshot snapshot){

        Supplier<TreeMap<Double,Double>> reverseTreeMapSupplier = ()-> new TreeMap<>(Comparator.reverseOrder());

        TreeMap<Double,Double> bids =
        snapshot.getBids().stream()
                .map(strArr -> new AbstractMap.SimpleEntry<>(Double.valueOf(strArr[0]), Double.valueOf(strArr[1])))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (u,v)-> u,   reverseTreeMapSupplier));

        TreeMap<Double,Double> asks =
                snapshot.getAsks().stream()
                .map(strArr -> new AbstractMap.SimpleEntry<>(Double.valueOf(strArr[0]), Double.valueOf(strArr[1])))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (u,v)-> u,   TreeMap::new));


        return OrderBook.builder()
                .productId(snapshot.getProduct_id())
                .bids(bids)
                .asks(asks)
                .build();

    }


    /**
     * Create a new copy of the OrderBook from an existing OrderBook with update applied.
     * @param existingOrderBook - An existing OrderBook.
     * @param l2Update - An update message.
     * @return - A new OrderBook after update.
     */
    public static OrderBook from(OrderBook existingOrderBook, L2Update l2Update){
        //Create a deep copy before updating to avoid the race conditions.
        OrderBook newOrderBook = existingOrderBook.withAsks(new TreeMap<>(existingOrderBook.asks))
                        .withBids(new TreeMap<>(existingOrderBook.bids))
                                .withProductId(existingOrderBook.productId);

        l2Update.getBids()
                .forEach(orderLine -> {
                    if(orderLine.getQuantity() == 0){
                        newOrderBook.bids.remove(orderLine.getPrice());
                    }else {
                        newOrderBook.bids.compute(orderLine.getPrice(), (existingPrice, existingQty)-> orderLine.getQuantity());
                    }

                });

        l2Update.getAsks()
                .forEach(orderLine -> {
                    if(orderLine.getQuantity() == 0){
                        newOrderBook.asks.remove(orderLine.getPrice());
                    }else {
                        newOrderBook.asks.compute(orderLine.getPrice(), (existingPrice, existingQty)-> orderLine.getQuantity());
                    }

                });
        return newOrderBook;
    }


    /**
     * Level 10 snapshot (the best 10 asks and best 10 bids)
     */
    public OrderBook getLevel10SnapShot(){
       Map<Double, Double> bids =  this.bids.entrySet()
                .stream()
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (u,v)-> u,  LinkedHashMap::new));
        Map<Double, Double> asks =  this.asks.entrySet()
                .stream()
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (u,v)-> u,  LinkedHashMap::new));

        return  OrderBook.builder()
                .productId(this.productId)
                .bids(bids)
                .asks(asks)
                .build();

    }


    public  Double getBestBidPrice() {
       return this.bids.keySet()
                .stream().findFirst().orElse(0.0);

    }

    public  Double getBestAskPrice() {
        return this.asks.keySet()
                .stream().findFirst().orElse(0.0);

    }
}
