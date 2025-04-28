import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class ClientReader {

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
            String requestMessage = "Read Last";
            channel.basicPublish(
                EXCHANGE_NAME,
                "",
                null,
                requestMessage.getBytes(StandardCharsets.UTF_8)
            );
            System.out.println(" [x] Sent 'Read Last' request.");

            channel.queueDeclare(RESPONSE_QUEUE, false, false, false, null);
            System.out.println(" [*] Waiting for first response...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String response = new String(
                    delivery.getBody(),
                    StandardCharsets.UTF_8
                );
                System.out.println(" [.] Received from replica: " + response);
                System.out.println(" [*] First response received. Exiting.");

                System.exit(0);
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
