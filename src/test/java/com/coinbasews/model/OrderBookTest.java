package com.coinbasews.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderBookTest {

    @Test
    void testFromSnapshot() {
        List<String[]> asks = List.of(new String[]{"100", "1.001"}, new String[]{"101.00", "0.09"},
                new String[]{"100.08", "1.001"}, new String[]{"101.90", "2.099"}
        );
        List<String[]> bids = List.of(new String[]{"99", "1.001"}, new String[]{"98.89", "0.09"},
                new String[]{"99.09", "2.201"}, new String[]{"97.90", "2.099"}
        );

        Snapshot snapshot = new Snapshot();
        snapshot.setAsks(asks);
        snapshot.setBids(bids);
        snapshot.setProduct_id("ETH-USD");

        OrderBook orderBook = OrderBook.from(snapshot);
        assertEquals(100, orderBook.getBestAskPrice());
        assertEquals(99.09, orderBook.getBestBidPrice());

    }

    @Test
    void testFromL2Update() {
        OrderBook orderBook = OrderBook.builder()
                .asks(new TreeMap<>(Map.of(100.00, 1.001, 101.00, 0.09, 100.08, 1.001, 101.90, 2.009)))
                .bids(new TreeMap<>(Map.of(99.00, 1.001, 98.89, 0.09, 99.09, 2.201, 97.90, 2.099)))
                .productId("ETH-USD")
                .build();

        assertEquals(4, orderBook.getAsks().size());
        assertEquals(4, orderBook.getBids().size());


        L2Update l2Update = new L2Update();
        l2Update.setChanges(Collections.singletonList(new String[]{"buy", "98.00", "1.90"}));
        l2Update.setType("l2update");
        l2Update.setProduct_id("ETH-USD");


        OrderBook newOrderBook = OrderBook.from(orderBook, l2Update);

        //Make sure order book size didn't change.

        assertEquals(4, orderBook.getAsks().size());
        assertEquals(4, orderBook.getBids().size());

        // The new order book should have an extra bid.

        assertEquals(5, newOrderBook.getBids().size());
        assertEquals(4, newOrderBook.getAsks().size());

        // A new update that change the quantity to 0.0

        L2Update l2Update2 = new L2Update();
        l2Update2.setChanges(Collections.singletonList(new String[]{"buy", "98.00", "0.00"}));
        l2Update2.setType("l2update");
        l2Update2.setProduct_id("ETH-USD");

        OrderBook newOrderBook2 = OrderBook.from(orderBook, l2Update2);
        // The newly added item removed the size is back to 4.
        assertEquals(4, newOrderBook2.getBids().size());
        assertEquals(4, newOrderBook2.getAsks().size());

        // A new ask to beat all
        L2Update l2Update3 = new L2Update();
        l2Update3.setChanges(Collections.singletonList(new String[]{"sell", "99.98", "0.90"}));
        l2Update3.setType("l2update");
        l2Update3.setProduct_id("ETH-USD");

        OrderBook newOrderBook3 = OrderBook.from(orderBook, l2Update3);
        assertEquals(99.98, newOrderBook3.getBestAskPrice());
        assertEquals(5, newOrderBook3.getAsks().size());


    }
}
