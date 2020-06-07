import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * @author: Kam-Chou
 * @date: 2020/6/7 18:23
 * @description: 发送方确认机制-生产者
 * @version: 1.0
 */
public class ConfirmProducer {

    private static Connection connection;
    private static Channel channel;

    public static void main(String[] args) {

    }

    public static void init() throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
         connection = RabbitConnectionFactory.getConnection(false);
         channel = connection.createChannel();
        channel.exchangeDeclare("confirm-exchange", "direct");
        channel.queueDeclare("confirm-queue", true, false, false, null);
        channel.queueBind("confirm-queue", "confirm-exchange", "confirm-key", null);
    }

    /**
     * 普通confirm
     * 如果信道没有开启 publisher confirm 模式，就会抛出异常.
     * waitForConfirms 如果收到了值，说明收到了 Ack/Nack, 需要等待服务器进行消息确认
     *
     * 事务机制和普通 confirm 的优点：不需要维护客户端中未确认的消息
     *
     * 注意：
     * 事务机制和 publisher confirm 机制是互斥的，两者共存会抛出异常.
     * 如果 exchange 没有对应的队列，那么消息会丢失.
     */
    public static void normalConfirm() {
        try {
            channel.confirmSelect();
            channel.basicPublish("confirm-exchange", "confirm-key", MessageProperties.PERSISTENT_TEXT_PLAIN, "Hello".getBytes());
            if (!channel.waitForConfirms()) {
                System.out.println("发送消息失败...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量confirm
     * 发送一批消息，调用 waitForConfirms 等待服务器的确认.
     *
     * 弊端：
     * 当出现返回 Nack 或超时情况时，客户端需要将这一次的所有消息重发.
     * 使用同步的方式等待服务端消息的确认，只要有一个未确认，都会抛出异常.
     *
     * 问题1：如何进行重新发送呢？
     * 这个其实是需要自己维护的，通过集合存储自己要发送的数据，然后需要的时候再发送一遍.
     */
    public static void batchConfirm() throws IOException, InterruptedException {
        try {
            int count = 0;
            channel.confirmSelect();
            while (true) {
                channel.basicPublish("confirm-exchange", "confirm-key", MessageProperties.PERSISTENT_TEXT_PLAIN, "Hello".getBytes());
                // 将发送的消息存入缓存(集合)

                // 如果数据发送完毕
                try {
                    if (++count > 10) {
                        if (channel.waitForConfirms()) {
                            // 将缓存清空
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // 重新发送缓存的数据
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 将缓存的数据重新发送
        }
    }

    /**
     * 异步confirm
     * 提供一个回调方法，服务端确认了一条或者多条消息后，客户端会回调该方法.
     * 当然，如果需要消息重发，那么也是需要自己维护一个缓存来存储数据.
     *
     * 好处：
     * 无需等待消息的确认，只要服务端确认了，那么回调机制就会被调用
     */
    public static void asyncConfirm() throws IOException {
        channel.confirmSelect();

        // 发送消息
        for (int i = 0; i < 10; i++) {
            channel.basicPublish("confirm-exchange", "confirm-key", MessageProperties.PERSISTENT_TEXT_PLAIN, "Hello".getBytes());
        }

        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("未确认消息，标识：" + deliveryTag);
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println(String.format("已确认消息，标识：%d，多个消息：%b", deliveryTag, multiple));
            }
        });
    }

}
