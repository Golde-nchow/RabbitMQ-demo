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
 * @date: 2020/6/3 23:13
 * @description: 生产者
 * @version: 1.0
 */
public class Producer {

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = null;
        Channel channel = null;
        try {
            connection = RabbitConnectionFactory.getConnection(false);
            // 创建一个管道，但是管道Channel类并非线程共享的，建议一个线程创建一个
            channel = connection.createChannel();

            // 交换器类型有4种，这一种是根据 bindingKey 和 routingKey 进行匹配。
            // 交换器的作用是接收生产者的消息，并根据交换器类型路由到不同队列。
            // exchange: 交换器
            // type:     交换器类型
            // durable:  是否持久化
            // autoDelete:  是否自动删除
            channel.exchangeDeclare("myExchanger", "direct", true);

            // 创建了一个非持久化、排他、自动删除的队列, 队列名称自动生成.
            // queue: 队列名称
            // durable: 是否持久化
            // exclusive: 是否排他
            // autoDelete: 是否自动删除.
            channel.queueDeclare("myQueue", false, false, false, null);
            // 通过 exchangeName 和 routingKey 定位到队列.
            channel.queueBind("myQueue", "myExchanger", "myRoutingKey");

            // 使用 Channel 进行发送消息
            // 可以设置消息的优先级、投递模式、内容类型
            // 可以发送带有 Header 的信息
            // 可以发送带有过期时间的消息
            byte[] messages = "Hello World".getBytes();
            channel.basicPublish("myExchanger", "myRoutingKey", MessageProperties.PERSISTENT_TEXT_PLAIN, messages);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.close();
            }
            if (connection != null) {
                connection.close();
            }
        }


    }

}
