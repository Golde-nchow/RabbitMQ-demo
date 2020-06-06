import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author: Kam-Chou
 * @date: 2020/6/6 16:30
 * @description: 延迟队列的生产者
 * @version: 1.0
 */
public class Producer {

    /**
     * 交换器
     */
    public static final String DELAY_EXCHANGE = "delay_exchanger";
    public static final String DLX_EXCHANGE = "dlx_exchanger";
    /**
     * 2个正常队列
     */
    public static final String NORMAL_QUEUE_1 = "normal_queue_1";
    public static final String NORMAL_QUEUE_2 = "normal_queue_2";
    /**
     * 正常队列的routingKey
     */
    public static final String NORMAL_KEY_1 = "normal_key_1";
    public static final String NORMAL_KEY_2 = "normal_key_2";
    /**
     * 2个死信队列
     */
    public static final String DLX_QUEUE_1 = "dlx_queue_1";
    public static final String DLX_QUEUE_2 = "dlx_queue_2";
    /**
     * 死信队列的routingKey
     */
    public static final String DLX_KEY_1 = "dlx_key_1";
    public static final String DLX_KEY_2 = "dlx_key_2";

    public static void main(String[] args) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        Connection connection = RabbitConnectionFactory.getConnection(false);
        Channel channel = connection.createChannel();

        // 发送消息
        sendMsg(new Date().toString(), 10, channel);
        // 消费消息
        Consumer.consumer_1(channel);
        Consumer.consumer_2(channel);
    }

    /**
     * 运行一次即可，用于初始化队列
     */
    public static void init(Channel channel) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        // 交换器绑定
        channel.exchangeDeclare(DELAY_EXCHANGE, "direct", true);
        channel.exchangeDeclare(DLX_EXCHANGE, "direct", true);
        // 普通队列绑定
        Map<String, Object> delay10s = new HashMap<>(4);
        delay10s.put("x-dead-letter-exchange", DLX_EXCHANGE);
        delay10s.put("x-dead-letter-routing-key", DLX_KEY_1);
        delay10s.put("x-message-ttl", 10000);
        Map<String, Object> delay60s = new HashMap<>(4);
        delay60s.put("x-dead-letter-exchange", DLX_EXCHANGE);
        delay60s.put("x-dead-letter-routing-key", DLX_KEY_2);
        delay60s.put("x-message-ttl", 60000);
        channel.queueDeclare(NORMAL_QUEUE_1, true, false, false, delay10s);
        channel.queueDeclare(NORMAL_QUEUE_2, true, false, false, delay60s);
        channel.queueBind(NORMAL_QUEUE_1, DELAY_EXCHANGE, NORMAL_KEY_1);
        channel.queueBind(NORMAL_QUEUE_2, DELAY_EXCHANGE, NORMAL_KEY_2);
        // 死信队列绑定
        channel.queueDeclare(DLX_QUEUE_1, true, false, false, null);
        channel.queueDeclare(DLX_QUEUE_2, true, false, false, null);
        channel.queueBind(DLX_QUEUE_1, DLX_EXCHANGE, DLX_KEY_1);
        channel.queueBind(DLX_QUEUE_2, DLX_EXCHANGE, DLX_KEY_2);
    }

    /**
     * 发送消息
     */
    public static void sendMsg(String msg, int delayTime, Channel channel) throws IOException, TimeoutException {
        switch (delayTime) {
            case 10:
                channel.basicPublish(DELAY_EXCHANGE, NORMAL_KEY_1, true, MessageProperties.TEXT_PLAIN, msg.getBytes());
                break;
            case 60:
                channel.basicPublish(DELAY_EXCHANGE, NORMAL_KEY_2, true, MessageProperties.TEXT_PLAIN, msg.getBytes());
                break;
            default:
                break;
        }
    }
}
