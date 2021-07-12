package com.alipay.sofa.registry.jraft.config.other;

import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;

/**
 * @author : xingpeng
 * @date : 2021-07-11 16:02
 **/
public class RheaKVDriverConfigBean implements RheaKVDriver{
    private RheaKVStoreOptions rheaKVStoreOptions;

    public void setRheaKVStoreOptions(RheaKVStoreOptions rheaKVStoreOptions) {
        this.rheaKVStoreOptions = rheaKVStoreOptions;
    }

    @Override
    public RheaKVStoreOptions getRheaKVStoreOptions() {
        return null;
    }
}
