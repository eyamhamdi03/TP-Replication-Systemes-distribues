import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class ClientReaderV2 {

    private static final String EXCHANGE_NAME = "replication_exchange";
    private static final String RESPONSE_QUEUE = "client_reader_response";

    public static void main(String[] args)
        throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()
        ) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String requestMessage = "Read All";
            channel.basicPublish(
                EXCHANGE_NAME,
                "",
                null,
                requestMessage.getBytes(StandardCharsets.UTF_8)
            );
            System.out.println(" [x] Sent 'Read All' request.");

            channel.queueDeclare(RESPONSE_QUEUE, false, false, false, null);
            System.out.println(" [*] Waiting for responses...");

            Map<String, Integer> lineCounts = new HashMap<>();
            Set<String> endSignals = new HashSet<>();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String response = new String(
                    delivery.getBody(),
                    StandardCharsets.UTF_8
                );

                if (response.startsWith("END_REPLICA_")) {
                    endSignals.add(response);
                } else {
                    lineCounts.put(
                        response,
                        lineCounts.getOrDefault(response, 0) + 1
                    );
                }

                if (endSignals.size() == 3) {
                    System.out.println("\n [*] Final Majority Lines:");

                    for (Map.Entry<
                        String,
                        Integer
                    > entry : lineCounts.entrySet()) {
                        if (entry.getValue() >= 2) { // majority of three is considered 2
                            System.out.println(" > " + entry.getKey());
                        }
                    }
                    System.exit(0);
                }
            };

            channel.basicConsume(
                RESPONSE_QUEUE,
                true,
                deliverCallback,
                consumerTag -> {}
            );

            while (true) {
                Thread.sleep(100);
            }
        }
    }
}
