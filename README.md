# coinbase-websocket-client
A Java websocket client that process the messages from Coinbase pro ws API

### Prerequisites
 - Java 11
 - Maven 3+

###  How to run 

clone the repo and run below commands
```
mvn clean install
java -jar target/coinbase-ws-client.jar <product id1> <product id2>
Eg :- java -jar target/coinbase-ws-client.jar ETH-USD BTC-USD
```
Press CtrL + C to stop.
