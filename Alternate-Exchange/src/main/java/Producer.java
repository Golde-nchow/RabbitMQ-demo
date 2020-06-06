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
     * alternate-exchanger 会覆盖 mandatory 配置.
     */
    public static void main(String[] args) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        Connection connection = RabbitConnectionFactory.getConnection(false);
        Channel channel = connection.createChannel();

        Map<String, Object> configs = new HashMap<String, Object>();
        // 如果需要使用备份交换器，那么就要设置这个属性，并指定对应的交换器
        configs.put("alternate-exchange", "alternate-exchanger");
        channel.exchangeDeclare("bad-exchanger", "direct", true, false, configs);
        // 备份交换器如果要设置 direct，那么一定要指定正确的队列.
        // fanout，发送到所有与该交换器相连的队列.
        channel.exchangeDeclare("alternate-exchanger", "fanout", true, false, null);

        channel.queueDeclare("bad-queue", true, false, false, null);
        channel.queueBind("bad-queue", "bad-exchanger", "bad-RoutingKey");
        channel.queueDeclare("alternate-queue", true, false, false, null);
        channel.queueBind("alternate-queue", "alternate-exchanger", "");

        byte[] messages = "Hello World".getBytes();
        // 这里为了测试 alternate-exchanger, 不设置 routingKey
        channel.basicPublish("alternate-exchanger", "", MessageProperties.TEXT_PLAIN, messages);

        channel.close();
        connection.close();
    }

}
