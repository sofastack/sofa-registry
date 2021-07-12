package com.alipay.sofa.registry.jraft.config.other;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author : xingpeng
 * @date : 2021-07-11 14:49
 **/
public class DefaultBeanManage implements BeanManage{
    public List result;

    public ApplicationContext applicationContext;

    @Override
    public List<RheaKVDriver> getBeanListOfType(Class<RheaKVDriver> clazz) {
        result=new ArrayList<>();
        Map<String, RheaKVDriver> map=applicationContext.getBeansOfType(clazz);
        if(map!=null && !map.isEmpty()){
            result.addAll(map.values());
        }
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
}
