package com.alipay.sofa.registry.jraft.config.other;

import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 * @author : xingpeng
 * @date : 2021-06-26 18:16
 **/
public interface BeanManage extends ApplicationContextAware {
    List<RheaKVDriver> getBeanListOfType(Class<RheaKVDriver> clazz);
    
}
    