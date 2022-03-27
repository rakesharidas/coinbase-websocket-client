import com.coinbasews.model.OrderBook;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class CoinbaseWsClientAppTest {

    @Test
    void testPublisher(){
        String snapshot = "{\"type\":\"snapshot\",\"product_id\":\"ETH-USD\"," +
                "\"asks\":[[\"2728.43\",\"0.25301986\"],[\"2728.44\",\"0.21399793\"]," +
                "[\"2728.48\",\"1.83277739\"],[\"2728.54\",\"2.93248100\"]," +
                "[\"2728.62\",\"0.17755521\"],[\"2728.63\",\"1.42565163\"]]," +
                "\"bids\":[[\"2727.99\",\"0.32131055\"],[\"2727.97\",\"1.67710829\"]," +
                "[\"2727.96\",\"1.37990254\"],[\"2727.86\",\"0.69784000\"]," +
                "[\"2727.83\",\"0.71000000\"],[\"2727.63\",\"1.44473280\"]]}";

        Flux<OrderBook> orderBookFlux = CoinbaseWsClientApp.createOrderBookPublisher(Flux.just(snapshot));

        StepVerifier.create(orderBookFlux)
                .expectNextMatches(orderBook -> (orderBook.getBestAskPrice() == 2728.43) && (orderBook.getBestBidPrice()==2727.99))
                .verifyComplete();
    }
}
