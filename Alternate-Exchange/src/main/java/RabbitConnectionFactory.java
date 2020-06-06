import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * @author: Kam-Chou
 * @date: 2020/6/3 23:16
 * @description: RabbitMQ连接工厂类
 * @version: 1.0
 */
public class RabbitConnectionFactory {

    public static Connection getConnection(boolean uri) throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        ConnectionFactory factory = new ConnectionFactory();
        // 通过 URI 的方式建立连接
        if (uri) {
            factory.setUri("amqp://guest:guest@127.0.0.1:15672");
        } else {
            // 通过设置参数的方式
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setHost("127.0.0.1");
            factory.setVirtualHost("/");
            factory.setPort(5672);
        }
        return factory.newConnection();
    }

}
