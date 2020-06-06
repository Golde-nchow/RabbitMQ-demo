import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * @author: Kam-Chou
 * @date: 2020/6/5 15:13
 * @description: 生产者（发送消息设置Mandatory）
 * @version: 1.0
 */
public class Publisher {

    public static void main(String[] args) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        Connection connection = RabbitConnectionFactory.getConnection(false);
        Channel channel = connection.createChannel();
        channel.exchangeDeclare("mandatoryExchanger", "direct", true);
        channel.queueDeclare("mandatoryQueue", false, false, false, null);
        channel.queueBind("mandatoryQueue", "mandatoryExchanger", "mandatoryRoutingKey");
        byte[] messages = "test Mandatory...".getBytes();

        // 为了测试 mandatory, 不设置路由键
        // 如果利用 exchange 和 routingKey 都找不到对应的队列，mandatory 为 true, 则返回到生产者，否则丢弃.
        channel.basicPublish("mandatoryExchanger", "", true, MessageProperties.TEXT_PLAIN, messages);
        channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) ->
                System.out.println(">>>>>>>> 返回的结果是: " + new String(body))
        );

        channel.close();
        connection.close();
    }

}
