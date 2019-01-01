package com.tre.stream.binder.cmq.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 */
@ConfigurationProperties("spring.cloud.stream.cmq.binder")
public class CmqBinderConfigurationProperties {
    private String secretId;
    private String secretKey;
    private String endpoint;

    public String getSecretId() {
        return secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
