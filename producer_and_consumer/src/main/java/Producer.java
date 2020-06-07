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

    /**
     * 什么是持久化 durable ?
     * 就是在 RabbitMQ 出现重启、关闭或宕机的时候，防止数据丢失.
     * 可持久化的组件: 交换器、队列、消息.
     * 但是不保证100%能够存入磁盘，这种情况要依赖镜像队列机制.
     */
    public static void main(String[] args) throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {

        Connection connection = RabbitConnectionFactory.getConnection(false);
        // 创建一个管道，但是管道Channel类并非线程共享的，建议一个线程创建一个
        Channel channel = connection.createChannel();

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

        // 在该 Channel 进行发送消息
        // 可以设置消息的优先级、投递模式、内容类型
        // 可以发送带有 Header 的信息
        // 可以发送带有过期时间的消息
        // PERSISTENT_TEXT_PLAIN: 包含对消息的持久化的功能
        byte[] messages = "Hello World".getBytes();
        channel.basicPublish("myExchanger", "myRoutingKey", MessageProperties.PERSISTENT_TEXT_PLAIN, messages);

        channel.close();
        connection.close();
    }

}
