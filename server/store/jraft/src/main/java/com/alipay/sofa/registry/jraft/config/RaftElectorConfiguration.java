package com.alipay.sofa.registry.jraft.config;

import com.alipay.sofa.registry.jraft.elector.MetaRaftLeaderElector;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import com.alipay.sofa.registry.store.api.spring.SpringContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : xingpeng
 * @date : 2021-07-06 16:16
 **/
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(
        value = SpringContext.PERSISTENCE_PROFILE_ACTIVE,
        havingValue = SpringContext.META_STORE_API_RAFT)
public class RaftElectorConfiguration {
    @Configuration
    public static class RaftElectorBeanConfiguration {
        @Bean
        public LeaderElector leaderElector() {
            return new MetaRaftLeaderElector();
        }
    }
}
