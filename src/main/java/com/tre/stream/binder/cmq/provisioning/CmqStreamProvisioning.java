package com.tre.stream.binder.cmq.provisioning;

import com.qcloud.cmq.*;
import com.tre.stream.binder.cmq.properties.CmqConsumerProperties;
import com.tre.stream.binder.cmq.properties.CmqProducerProperties;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmqStreamProvisioning implements
        ProvisioningProvider<ExtendedConsumerProperties<CmqConsumerProperties>,
                            ExtendedProducerProperties<CmqProducerProperties>> {

    private Account account;

    public CmqStreamProvisioning(Account account) {
        this.account = account;
    }

    /**
     * 在云上cmq创建队列，用于生产者生产
     * @param name bindings的name
     * @param properties
     * @return
     * @throws ProvisioningException
     */
    public ProducerDestination provisionProducerDestination(String name, ExtendedProducerProperties<CmqProducerProperties> properties){
        List<String> tempList = new ArrayList<String>();
        try {
            account.listTopic("", tempList, 0 ,0);
        } catch (Exception e) {
            throw new ProvisioningException(String.format("list topics on cmq error!"));
        }

        if (!tempList.contains(name)){
            throw new ProvisioningException(String.format("topic %s has not been created yet!", name));
        }

        return new CmqProducerDestination(name);
    }

    /**
     * 监听云上的cmq队列
     * @param name
     * @param group
     * @param properties
     * @return
     * @throws ProvisioningException
     */
    public ConsumerDestination provisionConsumerDestination(String name, String group,
                                                            ExtendedConsumerProperties<CmqConsumerProperties> properties)
            throws ProvisioningException {
        List<String> tempList = new ArrayList<String>();

        // 创建topic
        try {
            account.listTopic("", tempList, 0, 0);
        } catch (Exception e) {
            if(e instanceof CMQServerException)
                throw new ProvisioningException(String.format("list topic %s error: %s",
                        name, ((CMQServerException)e).getErrorMessage()));
        }
        if (!tempList.contains(name)) {
            createDestination(name, properties);
        }

        // 创建bindings，区别默认队列和group队列
        // 因为目前cmq不支持临时队列，没有配置group的consumer都属于一个默认队列。
        String queueName = "";
        if (null == group) {
            // 默认队列
            queueName = "default";
        }else{
            // group队列
            queueName = name + '_' + group;
        }

        // 判断是否有partition
        if (properties.isPartitioned()){
            queueName += "_" + properties.getInstanceIndex();
        }

        QueueMeta meta = new QueueMeta();
        meta.pollingWaitSeconds = 10;
        meta.visibilityTimeout = 30;
        meta.maxMsgSize = 65536;
        meta.msgRetentionSeconds = 345600;

        try {
            List<String> list = new ArrayList<String>();
            account.listQueue("", 0, 0, tempList);
            if (!list.contains(queueName)){
                account.createQueue(queueName, meta);
                if (properties.isPartitioned()){
                    // 创建topic时，要设置binding key
                    String bindingKey = name + "-" + properties.getInstanceIndex();
                    account.createSubscribe(name, queueName, queueName, "queue", null,
                            Collections.singletonList(bindingKey), "BACKOFF_RETRY", "JSON");
                }else{
                    account.createSubscribe(name, queueName, queueName, "queue");
                }

            }

        } catch (Exception e) {
            if(e instanceof CMQServerException)
                throw new ProvisioningException(String.format("create queue %s error: %s",
                        name, ((CMQServerException)e).getErrorMessage()));
        }

        return new CmqConsumerDestination(queueName);
    }

    private void createDestination(String name, ConsumerProperties properties){
        // 这里create topic，只能配置构造函数里的几个属性
        try {
            // 这个参数需要加到properties
            // 2：表示通过通过routing key过滤。sdk应该定义出常量。
            account.createTopic(name, 64 * 1024, 2);
        } catch (Exception e) {
            if(e instanceof CMQServerException)
                throw new ProvisioningException(String.format("create topic %s error: %s",
                        name, ((CMQServerException)e).getErrorMessage()));
            else
                throw new ProvisioningException(String.format("create topic %s error: %s", name, e.getMessage()));
        }
    }
}
