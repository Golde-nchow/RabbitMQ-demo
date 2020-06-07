import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * @author: Kam-Chou
 * @date: 2020/6/7 9:47
 * @description: RPC客户端，用于发送请求，取回消息
 * @version: 1.0
 */
public class Client {

    private Connection connection;
    private Channel channel;
    private String rpcQueue = "rpc-queue";
    private String responseQueue = "response-queue";
    private DefaultConsumer consumer;

    /**
     * 进行Connection和Channel的初始化
     */
    public Client() throws IOException, URISyntaxException, TimeoutException, NoSuchAlgorithmException, KeyManagementException {
        connection = RabbitConnectionFactory.getConnection(false);
        channel = connection.createChannel();
        channel.queueDeclare(responseQueue, false, false, false, null);
        consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("【客户端】获取消息: " + new String(body));
            }
        };
        channel.basicConsume(responseQueue, true, consumer);
    }

    /**
     * 声明 corrId, 发送请求
     */
    public void call(String message) throws IOException {
        String corrId = UUID.randomUUID().toString();
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .correlationId(corrId)
                .replyTo(responseQueue)
                .build();
        channel.basicPublish("", rpcQueue, properties, message.getBytes());
    }

    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    public static void main(String[] args) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        Client client = new Client();
        System.out.println("【客户端】正在请求信息...");
        client.call("Hello");
        client.close();
    }
}
