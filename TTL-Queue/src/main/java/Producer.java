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
 * @description: 生产者（配合过期的消息）
 * @version: 1.0
 */
public class Producer {

    /**
     * alternate-exchanger 会覆盖 mandatory 配置.
     */
    public static void main(String[] args) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        Connection connection = RabbitConnectionFactory.getConnection(false);
        Channel channel = connection.createChannel();
        channel.exchangeDeclare("myExchanger", "direct", true, false, null);

        // 可以控制未被使用的【队列的过期时间】
        // 未被使用条件包括: 没有任何消费者, 没有被重新声明, 没有使用 Basic.Get
        // 如果已存在该队列，需要先删除，配置无法覆盖.
        Map<String, Object> queueConfigs = new HashMap<String, Object>();
        queueConfigs.put("x-expires", 10000);
        channel.queueDeclare("myQueue", true, false, false, queueConfigs);
        channel.queueBind("myQueue", "myExchanger", "myRoutingKey");

        byte[] messages = "Expire Message".getBytes();
        channel.basicPublish("myExchanger", "myRoutingKey", MessageProperties.TEXT_PLAIN, messages);

        channel.close();
        connection.close();
    }

    /**
     * 【过期消息】会立马被删除, 因为过期的消息都在队列头部，RabbitMQ 只要定期检查头部即可.
     */
    public static AMQP.BasicProperties builderMethod() {
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        // 持久化消息
        builder.deliveryMode(2);
        builder.expiration("10000");
        return builder.build();
    }

}
