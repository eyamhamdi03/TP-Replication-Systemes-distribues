import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.Scanner;

public class ClientWriter {

    private static final String QUEUE_NAME = "replication_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()
        ) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            Scanner scanner = new Scanner(System.in);
            System.out.println(
                "Entrez les lignes a ajouter (tapez 'exit' pour quitter):"
            );
            int lineNumber = 1;
            while (true) {
                String line = scanner.nextLine();
                if ("exit".equalsIgnoreCase(line)) {
                    break;
                }
                String message = lineNumber + "  " + line;
                channel.basicPublish(
                    "",
                    QUEUE_NAME,
                    null,
                    message.getBytes("UTF-8")
                );
                System.out.println(" [x] Envoyé '" + message + "'");
                lineNumber++;
            }
        }
    }
}
