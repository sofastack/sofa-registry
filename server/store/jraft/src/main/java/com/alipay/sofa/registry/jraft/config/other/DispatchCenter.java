package com.alipay.sofa.registry.jraft.config.other;

import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author : xingpeng
 * @date : 2021-07-05 11:11
 **/
public class DispatchCenter{
    public BeanManage beanManage;

    public RheaKVDriver rheaKVDriver;

    public RheaKVStoreOptions rheaKVStoreOptions;

    private int beanListSize;

    public void init(){
        List<RheaKVDriver> beanListOfType = beanManage.getBeanListOfType(RheaKVDriver.class);
        beanListSize=beanListOfType.size();

    }

    public void setBeanManage(BeanManage beanManage) {
        this.beanManage = beanManage;
    }

    public void setRheaKVDriver(RheaKVDriver rheaKVDriver) {
        this.rheaKVDriver = rheaKVDriver;
    }

    public RheaKVStoreOptions getRheaKVStoreOptions() {
        return rheaKVStoreOptions;
    }
}
