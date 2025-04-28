import com.rabbitmq.client.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Replica {

    private static final String EXCHANGE_NAME = "replication_exchange";
    private static final String RESPONSE_QUEUE = "client_reader_response";

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java Replica <replica_number>");
            System.exit(1);
        }

        String replicaNumber = args[0];
        String directoryPath = "replica_" + replicaNumber;
        String filePath = directoryPath + "/file.txt";

        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setAutomaticRecoveryEnabled(true);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String queueName = "replica" + replicaNumber + "_queue";
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        channel.queueDeclare(RESPONSE_QUEUE, false, false, false, null);

        System.out.println(" [*] Waiting for messages in Replicas ");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(
                delivery.getBody(),
                StandardCharsets.UTF_8
            );
            System.out.println("[x] Received :" + message);

            if (message.equals("Read Last")) {
                String lastLine = getLastLine(filePath);
                System.out.println(" [.] Sending last line: " + lastLine);

                channel.basicPublish(
                    "",
                    RESPONSE_QUEUE,
                    null,
                    ("Replica " + replicaNumber + ": " + lastLine).getBytes(
                            StandardCharsets.UTF_8
                        )
                );
            } else if (message.equals("Read All")) {
                List<String> allLines = readAllLines(filePath);
                for (String line : allLines) {
                    channel.basicPublish(
                        "",
                        "client_reader_response",
                        null,
                        line.getBytes(StandardCharsets.UTF_8)
                    );
                }
                channel.basicPublish(
                    "",
                    "client_reader_response",
                    null,
                    ("END_REPLICA_" + replicaNumber).getBytes(
                            StandardCharsets.UTF_8
                        )
                );
            } else {
                try (FileWriter writer = new FileWriter(filePath, true)) {
                    writer.write(message + "\n");
                } catch (IOException e) {
                    System.err.println(
                        "Error writing to file: " + e.getMessage()
                    );
                }
            }
        };

        channel.basicConsume(
            queueName,
            true,
            deliverCallback,
            consumerTag -> {}
        );

        while (true) {
            Thread.sleep(1000);
        }
    }

    private static String getLastLine(String filePath) {
        try (
            java.io.RandomAccessFile file = new java.io.RandomAccessFile(
                filePath,
                "r"
            )
        ) {
            long length = file.length();
            if (length == 0) {
                return "(empty file)";
            }

            long pointer = length - 1;
            StringBuilder builder = new StringBuilder();
            while (pointer >= 0) {
                file.seek(pointer);
                char c = (char) file.read();
                if (c == '\n' && pointer != length - 1) {
                    break;
                }
                builder.insert(0, c);
                pointer--;
            }
            return builder.toString();
        } catch (IOException e) {
            return "(error reading)";
        }
    }

    private static List<String> readAllLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            lines.add("(error reading file)");
        }
        return lines;
    }
}
