import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientWriter {

    private static final String EXCHANGE_NAME = "replication_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()
        ) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Enter a line to write (or 'exit' to quit): ");
                String line = scanner.nextLine();
                if ("exit".equalsIgnoreCase(line)) {
                    break;
                }

                channel.basicPublish(
                    EXCHANGE_NAME,
                    "",
                    null,
                    line.getBytes(StandardCharsets.UTF_8)
                );
                System.out.println(" [x] Sent message: '" + line + "'");
            }
        }
    }
}
