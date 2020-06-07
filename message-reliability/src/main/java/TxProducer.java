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
 * @date: 2020/6/7 17:44
 * @description: 事务机制生产者
 * @version: 1.0
 */
public class TxProducer {

    /**
     * 虽然说事务可以解决发送方和RabbitMQ之间消息确认的问题，但是会降低RabbitMQ的性能.
     * 还有一种方式，避免性能上的损失，那就是发送方确认机制.
     */
    public static void main(String[] args) throws URISyntaxException, KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException {
        Connection connection = RabbitConnectionFactory.getConnection(false);
        Channel channel = connection.createChannel();
        channel.exchangeDeclare("tx-exchange", "direct");
        channel.queueDeclare("tx-queue", true, false, false, null);
        channel.queueBind("tx-queue", "tx-exchange", "tx-key", null);

        try {
            // 将该信道设置为事务模式
            channel.txSelect();
            channel.basicPublish("tx-exchange", "tx-key", MessageProperties.PERSISTENT_TEXT_PLAIN, "Hello".getBytes());
            System.out.println(1 / 0);
            // 进行事务的提交，若需要发送多条数据，需要多次调用 basicPublish 和 txCommit.
            // 只有RabbitMQ接收到后，才能成功提交，才能继续发送下一条消息，否则会阻塞.
            channel.txCommit();
        } catch (Exception e) {
            // 进行事务的回滚.
            channel.txRollback();
        }
    }

}
