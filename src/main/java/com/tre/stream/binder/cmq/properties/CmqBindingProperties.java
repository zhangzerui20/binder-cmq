package com.tre.stream.binder.cmq.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

public class CmqBindingProperties implements BinderSpecificPropertiesProvider {

    private CmqConsumerProperties consumerProperties = new CmqConsumerProperties();
    private CmqProducerProperties producerProperties = new CmqProducerProperties();


    public Object getConsumer() {
        return consumerProperties;
    }

    public Object getProducer() {
        return producerProperties;
    }

    public void setConsumer(CmqConsumerProperties consumerProperties) {
        this.consumerProperties = consumerProperties;
    }

    public void setProducer(CmqProducerProperties producerProperties) {
        this.producerProperties = producerProperties;
    }
}
