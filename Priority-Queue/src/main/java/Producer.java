import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author: Kam-Chou
 * @date: 2020/6/5 15:51
 * @description: 生产者（配合备份交换器）
 * @version: 1.0
 */
public class Producer {

    /**
     * 如果消费者的消费速度，大于生产速度，那么设置优先级是毫无用处的.
     */
    public static void main(String[] args) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        Connection connection = RabbitConnectionFactory.getConnection(false);
        Channel channel = connection.createChannel();

        // 设置这个队列消息的最大优先级为 10
        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put("x-max-priority", 10);
        channel.exchangeDeclare("normal-exchanger", "direct", true, false, configs);
        channel.queueDeclare("normal-queue", true, false, false, null);
        channel.queueBind("normal-queue", "normal-exchanger", "normal-RoutingKey");

        byte[] message1 = "World".getBytes();
        byte[] message2 = "Hello".getBytes();

        // 设置消息的优先级
        // 预期结果：Hello World
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().priority(2).build();
        AMQP.BasicProperties properties1 = new AMQP.BasicProperties.Builder().priority(4).build();
        channel.basicPublish("normal-exchanger", "normal-RoutingKey", properties , message1);
        channel.basicPublish("normal-exchanger", "normal-RoutingKey", properties1 , message2);

        // 消费
        Consumer.consume(channel);
        Consumer.consume(channel);
    }

}
