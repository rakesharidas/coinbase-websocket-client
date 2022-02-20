import com.coinbasews.endpoint.ClientEndPoint;
import com.coinbasews.model.Message;
import com.coinbasews.model.OrderBook;
import com.coinbasews.service.OrderBookProcessor;
import com.coinbasews.util.FluxSinkConsumer;
import com.coinbasews.util.JsonSerializeUtil;
import org.glassfish.tyrus.client.ClientManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
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
     * Pipeline to asynchronously process the messages.
     * @param fluxSinkConsumer  - A FluxSink Consumer that stream messages.
     * @return -
     */
    static Flux<OrderBook> getPublisher(FluxSinkConsumer fluxSinkConsumer){
        return Flux.create(fluxSinkConsumer)
                .delayElements(Duration.ofSeconds(1)) // Add a minor delay.
                // Asynchronously process each message.
                .flatMap(message-> Mono.fromCallable(()-> OrderBookProcessor.getInstance().processMessage(message))
                        .subscribeOn(Schedulers.boundedElastic()))
                // Log the message and continue with message.
                .onErrorContinue((throwable,msg) -> System.err.println("Error occurred for message : " + msg + " And the error is : "   + throwable)
                );
    }

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

    public static void main(String[] args) {

        List<String> products =  Arrays.stream(args).collect(Collectors.toList());

        if(products.isEmpty()){
            throw new RuntimeException(" Provide products/instrument for which you need the order book. The right usage is java -jar websocket-client.jar ETH-USD BTC-USD ");
        }

        CountDownLatch latch = new CountDownLatch(1);
        ClientManager client = ClientManager.createClient();
        try {
            String prodURL = "wss://ws-feed.exchange.coinbase.com";

            URI uri = new URI(prodURL);


            FluxSinkConsumer fluxSinkConsumer = new FluxSinkConsumer();

           getPublisher(fluxSinkConsumer)
                .subscribe(str -> System.out.println("OrderBook = " + str),
                        err-> System.err.println("Error occurred" + err)
                );


            Session session = client.connectToServer(new ClientEndPoint(fluxSinkConsumer), uri);


            session.getBasicRemote().sendText(JsonSerializeUtil.getJsonString(createRequestMessage(products)));

            /** This handler will be called on Control-C pressed */
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        session.close();
                        latch.countDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            latch.await();
        } catch (URISyntaxException | InterruptedException | DeploymentException | IOException e) {
            e.printStackTrace();
        }
    }
}

