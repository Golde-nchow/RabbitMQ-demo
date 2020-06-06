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
 * @date: 2020/6/5 15:13
 * @description: 生产者（发送消息设置Mandatory）
 * @version: 1.0
 */
public class Publisher {

    /**
     * 死信队列，用于当一个队列的消息不可用之后，那么变成死信，这时候可以把它重新发送到另一个队列用于保存.
     * 存储死信的队列就叫做死信队列.
     */
    public static void main(String[] args) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        Connection connection = RabbitConnectionFactory.getConnection(false);
        Channel channel = connection.createChannel();
        channel.exchangeDeclare("DLX-exchange", "direct", true);
        channel.exchangeDeclare("TTL-exchange", "direct", true);

        // 死信队列
        channel.queueDeclare("DLX-queue", false, false, false, null);
        channel.queueBind("DLX-queue", "DLX-exchange", "DLX-routingKey");

        // 普通队列
        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put("x-message-ttl", 10000);
        configs.put("x-dead-letter-exchange", "DLX-exchange");
        // 这里的 routingKey 对应死信队列的 routingKey
        configs.put("x-dead-letter-routing-key", "DLX-routingKey");
        channel.queueDeclare("TTL-queue", false, false, false, configs);
        channel.queueBind("TTL-queue", "TTL-exchange", "TTL-routingKey");

        byte[] messages = "test Mandatory...".getBytes();
        channel.basicPublish("TTL-exchange", "TTL-routingKey", true, MessageProperties.TEXT_PLAIN, messages);

        channel.close();
        connection.close();
    }

}
