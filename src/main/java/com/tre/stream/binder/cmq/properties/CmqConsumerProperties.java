package com.tre.stream.binder.cmq.properties;

public class CmqConsumerProperties extends CmqCommonProperties {
    private int pollingWaitSeconds;

    public int getPollingWaitSeconds() {
        return pollingWaitSeconds;
    }

    public void setPollingWaitSeconds(int pollingWaitSeconds) {
        this.pollingWaitSeconds = pollingWaitSeconds;
    }
}
