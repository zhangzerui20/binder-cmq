package com.tre.stream.binder.cmq.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

@ConfigurationProperties("spring.cloud.stream.cmq")
public class CmqExtendedBindingProperties
        extends AbstractExtendedBindingProperties<CmqConsumerProperties, CmqProducerProperties, CmqBindingProperties> {

    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.cmq.default";

    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return CmqBindingProperties.class;
    }
}
