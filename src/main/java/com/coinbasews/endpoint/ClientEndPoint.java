package com.coinbasews.endpoint;

import com.coinbasews.util.FluxSinkConsumer;

import javax.websocket.*;

/**
 * A Client end point listens to web socket messages.
 */
@ClientEndpoint
public class ClientEndPoint {

    private FluxSinkConsumer fluxSinkConsumer;

    public ClientEndPoint(FluxSinkConsumer fluxSinkConsumer) {
        this.fluxSinkConsumer = fluxSinkConsumer;
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected : " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        fluxSinkConsumer.publish(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("Session " + session.getId() + " closed because " + closeReason);
    }


}
