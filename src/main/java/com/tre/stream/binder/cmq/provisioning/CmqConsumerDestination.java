package com.tre.stream.binder.cmq.provisioning;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;

/**
 * CmqConsumerDestination提供链接到cmq的queue的信息
 */
public class CmqConsumerDestination implements ConsumerDestination {

    private String destinationName;

    public CmqConsumerDestination(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getName() {
        return destinationName;
    }
}
