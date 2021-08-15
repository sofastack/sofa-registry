package com.alipay.sofa.registry.jraft.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaojian.xj
 * @version $Id: DefaultCommonConfig.java, v 0.1 2021年03月22日 21:05 xiaojian.xj Exp $
 */
@ConfigurationProperties(prefix = MetaElectorConfigBean.PRE_FIX)
public class MetaElectorConfigBean implements MetaElectorConfig {
    public static final String PRE_FIX = "meta.server.elector";

    private long lockExpireDuration = 20 * 1000;

    @Override
    public long getLockExpireDuration() {
        return lockExpireDuration;
    }

    /**
     * Setter method for property <tt>lockExpireDuration</tt>.
     *
     * @param lockExpireDuration value to be assigned to property lockExpireDuration
     */
    public void setLockExpireDuration(long lockExpireDuration) {
        this.lockExpireDuration = lockExpireDuration;
    }
}
