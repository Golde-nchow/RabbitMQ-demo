import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * @author: Kam-Chou
 * @date: 2020/6/7 10:25
 * @description: RPC服务器，用于处理请求，然后返回客户端
 * @version: 1.0
 */
public class Server {

    private static final String RPC_QUEUE = "rpc-queue";

    public static void main(String[] args) throws IOException, URISyntaxException, TimeoutException, NoSuchAlgorithmException, KeyManagementException {
        Connection connection = RabbitConnectionFactory.getConnection(false);
        Channel channel = connection.createChannel();
        channel.queueDeclare(RPC_QUEUE, false, false, false, null);
        channel.basicQos(1);

        System.out.println("【服务端】正在等待 RPC 请求...");
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                AMQP.BasicProperties responseProp = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();

                String response;
                String receiver = new String(body);
                System.out.println("【服务端】收到请求：" + receiver);
                if ("Hello".equals(receiver)) {
                    response = "World";
                } else {
                    response = "Error";
                }

                channel.basicPublish("", properties.getReplyTo(), responseProp, response.getBytes("UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        channel.basicConsume(RPC_QUEUE, false, consumer);
    }

}
