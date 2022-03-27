package com.coinbasews.service;

import com.coinbasews.model.L2Update;
import com.coinbasews.model.OrderBook;
import com.coinbasews.model.Snapshot;
import com.coinbasews.util.JsonSerializeUtil;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to process and messages and store.
 */
public class OrderBookProcessor {

    private final ConcurrentHashMap<String, OrderBook> orderBookStore = new ConcurrentHashMap<>();


    private static OrderBookProcessor orderBookProcessor;

    public static synchronized OrderBookProcessor getInstance() {
        if (orderBookProcessor == null) {
            orderBookProcessor = new OrderBookProcessor();
        }
        return orderBookProcessor;
    }

    /**
     * Process the message and return a level 10 snapshot of orderBook.
     * @param message - message to process
     * @return - A new OrderBook updated or created with the new message.
     */
    public OrderBook processMessage(String message) {
        if (message.contains("\"type\":\"l2update\"")) {
            L2Update l2Update = processL2Update(message);
            return orderBookStore.get(l2Update.getProduct_id()).getLevel10SnapShot();
        } else if (message.contains("\"type\":\"snapshot\"")) {
            Snapshot snapshot =  processSnapshot(message);
            return orderBookStore.get(snapshot.getProduct_id()).getLevel10SnapShot();
        }else {
            throw new RuntimeException("Unexpected message which can't be processed by the system" + message);
        }
    }

    private Snapshot processSnapshot(String message) {
        Snapshot snapshot = JsonSerializeUtil.getTypeFromJson(message, Snapshot.class);
        OrderBook orderBook = OrderBook.from(snapshot);
        orderBookStore.putIfAbsent(snapshot.getProduct_id(), orderBook);
        return snapshot;
    }

    private L2Update processL2Update(String message) {
        L2Update l2Update = JsonSerializeUtil.getTypeFromJson(message, L2Update.class);
        orderBookStore.compute(l2Update.getProduct_id(), (productId, orderBook) -> OrderBook.from(orderBook, l2Update));
        return l2Update;
    }
}
