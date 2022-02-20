package com.coinbasews.util;

import reactor.core.publisher.FluxSink;

import java.util.function.Consumer;

/**
 * A Flux sink consumer that publish the messages received from the server.
 */
public class FluxSinkConsumer implements Consumer<FluxSink<String>> {

    private FluxSink<String> fluxSink;

    @Override
    public void accept(FluxSink<String> fluxSink) {
        this.fluxSink = fluxSink;
    }

    public void publish(String message){
        fluxSink.next(message);
    }
}
