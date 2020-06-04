import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * @author: Kam-Chou
 * @date: 2020/6/4 13:53
 * @description: 消费者
 * @version: 1.0
 */
public class Consumer {

    public static void main(String[] args) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        pull();
    }

    /**
     * 使用方式，因为推模式是采用持续订阅的方式，所以让生产者生产多个消息，然后再运行推模式，就能够消费多个消息
     * 但是无法一边发，一边接收，不是说好持续订阅的吗, 不明白.
     */
    public static void push() throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        // 如果设置了手动提交，那么只有当执行了ack之后，RabbitMQ 才会移除该消息.
        final boolean autoAck = false;
        Connection connection = RabbitConnectionFactory.getConnection(false);
        final Channel channel = connection.createChannel();

        channel.queueDeclare("myQueue", false, false, false, null);

        // 通过持续订阅的方式，来进行消费消息
        // 使用显式ack，能够防止消息丢失.
        // queue:       队列的名称
        // autoAck:     是否自动确认
        // consumerTag: 消费者标签，区分多个消费者
        // callback:    消费者的回调函数，可以使用 Consumer接口 或 DefaultConsumer类
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                long deliveryTag = envelope.getDeliveryTag();
                System.out.println(">>>>>>>> 接收到的消息: " + new String(body));
                channel.basicAck(deliveryTag, false);
            }
        };
        channel.basicConsume("myQueue", autoAck, "myConsumer", consumer);
        channel.close();
        connection.close();
    }

    /**
     * 拉模式
     * 只能单条地获取消息，但是不建议使用循环获取，如果要一次获取多条，还是使用推模式.
     */
    public static void pull() throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        Connection connection = RabbitConnectionFactory.getConnection(false);
        Channel channel = connection.createChannel();

        GetResponse response = channel.basicGet("myQueue", false);
        System.out.println(">>>>>>>> 接收到的消息: " + new String(response.getBody()));
        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);

        channel.close();
        connection.close();
    }

}
