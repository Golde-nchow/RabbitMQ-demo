import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * @author: Kam-Chou
 * @date: 2020/6/6 16:30
 * @description: 延迟队列的消费者
 * @version: 1.0
 */
public class Consumer {

    public static final String DLX_QUEUE_1 = "dlx_queue_1";
    public static final String DLX_QUEUE_2 = "dlx_queue_2";

    public static void main(String[] args) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        Connection connection = RabbitConnectionFactory.getConnection(false);
        Channel channel = connection.createChannel();
        consumer_1(channel);
        consumer_2(channel);
    }

    public static void consumer_1(Channel channel) throws IOException {
        channel.basicConsume(DLX_QUEUE_1, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(">>>>>>>> 死信队列1-返回的结果是: " + new String(body));
            }
        });
    }

    public static void consumer_2(Channel channel) throws IOException {
        channel.basicConsume(DLX_QUEUE_2, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(">>>>>>>> 死信队列2-返回的结果是: " + new String(body));
            }
        });
    }

}
