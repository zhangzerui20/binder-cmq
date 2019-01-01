package com.tre.stream.binder.cmq.config;

import com.qcloud.cmq.Account;
import com.tre.stream.binder.cmq.CmqChannelBinder;
import com.tre.stream.binder.cmq.properties.CmqBinderConfigurationProperties;
import com.tre.stream.binder.cmq.properties.CmqExtendedBindingProperties;
import com.tre.stream.binder.cmq.provisioning.CmqStreamProvisioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;


/**
 * 配置文件，配置访问云上cmq必要的属性、生产者消费者相关属性
 */
@Configuration
@EnableConfigurationProperties({CmqBinderConfigurationProperties.class, CmqExtendedBindingProperties.class})
public class CmqBinderConfiguration {

    @Autowired
    CmqBinderConfigurationProperties cmqBinderConfigurationProperties;

    @Autowired
    CmqExtendedBindingProperties cmqExtendedBindingProperties;

    @Autowired
    Account account;

    @Bean
    public CmqChannelBinder cmqChannelBinder(){

        CmqStreamProvisioning provisioning = new CmqStreamProvisioning(account);
        CmqChannelBinder channelBinder = new CmqChannelBinder(null, provisioning);
        channelBinder.setAccount(account);
        channelBinder.setExtendedBindingProperties(cmqExtendedBindingProperties);
        return channelBinder;
    }

    //todo 这里后面换成tcp sdk后需要重新封装一个对象
    @Bean
    public Account account(){
        String secretId = cmqBinderConfigurationProperties.getSecretId();
        String secretKey = cmqBinderConfigurationProperties.getSecretKey();
        String endpoint = cmqBinderConfigurationProperties.getEndpoint();

        if (StringUtils.isEmpty(secretId) || StringUtils.isEmpty(secretKey) || StringUtils.isEmpty(endpoint)){
            throw new IllegalArgumentException("secretId and secretKey must be set to connect cmq cloud service!");
        }
        return  new Account(endpoint, secretId, secretKey);
    }

}
