package com.tre.stream.binder.cmq;

import com.qcloud.cmq.Account;
import com.tre.stream.binder.cmq.binderCore.CmqInboundChannelAdapter;
import com.tre.stream.binder.cmq.properties.CmqConsumerProperties;
import com.tre.stream.binder.cmq.properties.CmqExtendedBindingProperties;
import com.tre.stream.binder.cmq.properties.CmqProducerProperties;
import com.tre.stream.binder.cmq.provisioning.CmqStreamProvisioning;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * 关键binder类，提供了app到mq的抽象。
 */
public class CmqChannelBinder extends
        AbstractMessageChannelBinder<ExtendedConsumerProperties<CmqConsumerProperties>,
                ExtendedProducerProperties<CmqProducerProperties>, CmqStreamProvisioning> implements
        ExtendedPropertiesBinder<MessageChannel, CmqConsumerProperties, CmqProducerProperties> {

    private Account account;
    private CmqExtendedBindingProperties extendedBindingProperties;

    public CmqChannelBinder(String[] headersToEmbed, CmqStreamProvisioning provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
    }

    /**
     * 创建发送消息的处理类
     * @param destination 生产者的destination
     * @param producerProperties 生产者属性
     * @param errorChannel
     * @return
     * @throws Exception
     */
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
                                                          ExtendedProducerProperties<CmqProducerProperties> producerProperties,
                                                          MessageChannel errorChannel) throws Exception {

        CmqMessageHandler messageHandler = new CmqMessageHandler(destination, producerProperties, account);
        messageHandler.setBeanFactory(this.getBeanFactory());
        return messageHandler;
    }

    /**
     * 启动任务，实例化adapter，从provision创建的queue中取消息，并交给用户
     * @param destination mq中的物理queue名
     * @param group 当前应用的group名
     * @param properties 消费者配置文件
     * @return
     * @throws Exception
     */
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination,
                                                     String group, ExtendedConsumerProperties<CmqConsumerProperties> properties) throws Exception {

        CmqInboundChannelAdapter adapter = new CmqInboundChannelAdapter(destination, group, properties.getExtension(), account);
        return adapter;
    }


    public CmqConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.extendedBindingProperties.getExtendedConsumerProperties(channelName);
    }

    public CmqProducerProperties getExtendedProducerProperties(String channelName) {
        return this.extendedBindingProperties.getExtendedProducerProperties(channelName);
    }

    public String getDefaultsPrefix() {
        return null;
    }

    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.extendedBindingProperties.getExtendedPropertiesEntryClass();
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setExtendedBindingProperties(CmqExtendedBindingProperties extendedBindingProperties) {
        this.extendedBindingProperties = extendedBindingProperties;
    }
}
