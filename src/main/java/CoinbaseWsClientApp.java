import com.coinbasews.model.Message;
import com.coinbasews.model.OrderBook;
import com.coinbasews.service.OrderBookProcessor;
import com.coinbasews.util.JsonSerializeUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Entry point class to Coinbase
 */
public class CoinbaseWsClientApp {

    /**
     * Create the request messsage to send to server.
     * @param products - collections of product ids / instruments to send.
     */
    static Message createRequestMessage(List<String> products){
        return Message.builder()
                .type("subscribe")
                .channels(Collections.singletonList("level2"))
                .product_ids(products)
                .build();
    }

    static Flux<OrderBook> createOrderBookPublisher(Flux<String> wsPublisher){
       return wsPublisher
        .delayElements(Duration.ofMillis(200))
             //  .
               //Asynchronously process message.
                .flatMap(wsMessage-> Mono.fromCallable(()-> OrderBookProcessor.getInstance().processMessage(wsMessage))
                        .subscribeOn(Schedulers.boundedElastic()))
                // Log the message and continue with message.
                .onErrorContinue((throwable,msg) -> System.err.println("Error occurred for message : " + msg + " And the error is : "   + throwable));
    }

    public static void main(String[] args) {

        List<String> products =  Arrays.stream(args).collect(Collectors.toList());

        if(products.isEmpty()){
            throw new RuntimeException(" Provide products/instrument for which you need the order book. The right usage is java -jar websocket-client.jar ETH-USD BTC-USD ");
        }

        CountDownLatch latch = new CountDownLatch(1);
        try {
            String prodURL = "wss://ws-feed.exchange.coinbase.com";

            URI uri = new URI(prodURL);

            HttpClient httpClient = HttpClient.create();
            String message = JsonSerializeUtil.getJsonString(createRequestMessage(products));

            httpClient.websocket(WebsocketClientSpec.builder()
                            .maxFramePayloadLength(16777216)
                            .build())
                    .uri(uri)
                    .handle((inBound, outBound)-> {
                        createOrderBookPublisher(inBound.receive().asString())
                                .subscribe(str-> System.out.println("OrderBook = " + str));
                        return outBound.sendString(Flux.just(message)).neverComplete();
                    }).blockLast();

            //This handler will be called on Control-C pressed
            Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));

            latch.await();
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

