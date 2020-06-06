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

    public static void consume(Channel channel) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        GetResponse response = channel.basicGet("normal-queue", false);
        System.out.println(">>>>>>>> 接收到的消息: " + new String(response.getBody()));
        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
    }

}
