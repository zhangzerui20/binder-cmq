package com.tre.stream.binder.cmq;

import com.qcloud.cmq.Account;
import com.qcloud.cmq.CMQServerException;
import com.qcloud.cmq.Queue;
import com.qcloud.cmq.Topic;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.handler.AbstractMessageProducingHandler;
import org.springframework.messaging.Message;

/**
 * 发送消息处理类
 */
public class CmqMessageHandler extends AbstractMessageProducingHandler {

    private ProducerDestination producerDestination;
    private ProducerProperties producerProperties;
    private Account account;

    public CmqMessageHandler(ProducerDestination producerDestination, ProducerProperties producerProperties, Account account) {
        this.producerDestination = producerDestination;
        this.producerProperties = producerProperties;
        this.account = account;
    }

    /**
     * 发送消息处理函数
     * @param message
     * @throws Exception
     */
    protected void handleMessageInternal(Message<?> message) throws Exception {
        sendMessage(message);
    }

    /**
     * 发布消息到cmq topic
     * @param message
     * @return
     */
    private void sendMessage(Message<?> message) throws Exception {
        Topic topic = account.getTopic(producerDestination.getName());
        try {
            if (producerProperties.getPartitionKeyExpression() != null){
                // partitioned的producer
                String msg = new String((byte[]) (message.getPayload()));
                Integer partition = (Integer) message.getHeaders().get("scst_partition");
                String routingKey = producerDestination.getName() + "-" + partition;
                topic.publishMessage(msg, routingKey);
            } else{

                topic.publishMessage(new String((byte[]) (message.getPayload())));
            }

        } catch (Exception e) {
            if (e instanceof CMQServerException){
                throw new Exception(((CMQServerException)e).getErrorMessage());
            }
        }
    }



}
