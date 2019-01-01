package com.tre.stream.binder.cmq.binderCore;

import com.qcloud.cmq.Account;
import com.qcloud.cmq.CMQServerException;
import com.qcloud.cmq.Queue;
import com.qcloud.cmq.QueueMeta;
import com.tre.stream.binder.cmq.properties.CmqConsumerProperties;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.core.AttributeAccessor;
import org.springframework.integration.context.OrderlyShutdownCapable;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;


/**
 * 接收消息，将消息转换为Spring Integration Messages的标准格式，
 * 并将消息发送到Message Channel中，触发StreamListener中的方法。
 *
 * 在这个类中可以设置接收消息的一些属性，例如是否自动ack，ack重试的次数等。
 */
public class CmqInboundChannelAdapter extends MessageProducerSupport implements
        OrderlyShutdownCapable {

    private ConsumerDestination destination;
    private String group;
    private CmqConsumerProperties properties;
    private Account account;
    private volatile boolean stopFlag;

    public CmqInboundChannelAdapter(ConsumerDestination destination, String group,
                                    CmqConsumerProperties properties, Account account) {
        this.destination = destination;
        this.group = group;
        this.properties = properties;
        this.account = account;
    }

    @Override
    protected void onInit() {
        super.onInit();
    }

    @Override
    protected void doStart() {
        super.doStart();

        ConsumerListener listener = new ConsumerListener(this);
        listener.setDestination(destination);
        listener.setGroup(group);
        listener.setStopFlag(stopFlag);
        listener.setAccount(account);

        Thread listenerThread = new Thread(listener);
        listenerThread.start();
    }

    @Override
    protected void doStop() {
        super.doStop();
        stopFlag = true;
    }

    @Override
    protected AttributeAccessor getErrorMessageAttributes(Message<?> message) {
        return super.getErrorMessageAttributes(message);
    }

    @Override
    public String getComponentType() {
        return super.getComponentType();
    }

    public int beforeShutdown() {
        return 0;
    }

    public int afterShutdown() {
        return 0;
    }


    /**
     * 执行具体接收消息任务的类
     */
    class ConsumerListener implements Runnable{

        private CmqInboundChannelAdapter adapter;
        private volatile boolean stopFlag;
        private ConsumerDestination destination;
        private String group;
        private Account account;

        public ConsumerListener(CmqInboundChannelAdapter adapter) {
            this.adapter = adapter;
        }

        public void run() {
            while (stopFlag != true){
                try {
                    Thread.sleep(5000);
                    adapter.sendMessage(receiveAndCreateMessage());
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (CMQServerException e) {
                    if (e.getErrorCode() == CloudCmqErrorCode.NO_MESSAGE_ERROR)
                        continue;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        private Message<?> receiveAndCreateMessage() throws Exception {
            // todo 根据destination得到bindings name
            Queue queue = account.getQueue(destination.getName());
            Message message = null;
            int pollingWaitSeconds = properties.getPollingWaitSeconds();
            com.qcloud.cmq.Message msg = queue.receiveMessage(
                    pollingWaitSeconds == 0 ? 10 : pollingWaitSeconds);
            message = MessageBuilder.withPayload(msg.msgBody).setHeader("msgId", msg.msgId)
                        .setHeader("nextVisibleTime", msg.nextVisibleTime).build();
            // todo 这里 auto delete，能否做到配置中
            queue.deleteMessage(msg.receiptHandle);
            return message;
        }

        public void setStopFlag(boolean stopFlag) {
            this.stopFlag = stopFlag;
        }

        public void setDestination(ConsumerDestination destination) {
            this.destination = destination;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public void setAccount(Account account) {
            this.account = account;
        }
    }


}
