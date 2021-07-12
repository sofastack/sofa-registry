package com.alipay.sofa.registry.jraft.config.other;

import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;

import java.util.List;

/**
 * @author : xingpeng
 * @date : 2021-07-01 22:43
 **/
public interface RheaKVDriver {
//    void CheckRheaKVDriverConfigBean(List<RheaKVDriver> beanList);
//
//    RheaKVDriver getRheaKVDriver();
    RheaKVStoreOptions getRheaKVStoreOptions();
}
