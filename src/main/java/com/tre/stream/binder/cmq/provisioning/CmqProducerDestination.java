package com.tre.stream.binder.cmq.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

/**
 * 保存destination信息
 */
public class CmqProducerDestination implements ProducerDestination {

    private String bindingsName;

    public CmqProducerDestination(String bindingsName) {
        this.bindingsName = bindingsName;
    }

    public String getName() {
        return bindingsName;
    }

    public String getNameForPartition(int partition) {
        return "test-partition";
    }
}
