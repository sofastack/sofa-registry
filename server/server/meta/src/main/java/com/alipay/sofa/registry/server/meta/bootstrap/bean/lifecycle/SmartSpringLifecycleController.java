package com.alipay.sofa.registry.server.meta.bootstrap.bean.lifecycle;

import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.lifecycle.SmartSpringLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */
@Component
public class SmartSpringLifecycleController implements BeanPostProcessor, DestructionAwareBeanPostProcessor, Ordered {

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if(!isSmartSpringLifecycleBean(bean)) {
            return;
        }
        try {
            LifecycleHelper.stopIfPossible(bean);
            LifecycleHelper.disposeIfPossible(bean);
        } catch (Exception e) {
            throw new SofaRegistryRuntimeException(String.format("[%s]%s", beanName, "Lifecycle dispose exception"), e);
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return true;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(!isSmartSpringLifecycleBean(bean)) {
            return bean;
        }
        try {
            LifecycleHelper.initializeIfPossible(bean);
            LifecycleHelper.startIfPossible(bean);
        } catch (Exception e) {
            throw new SofaRegistryRuntimeException(String.format("[%s]%s", beanName, "Lifecycle init exception"), e);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private boolean isSmartSpringLifecycleBean(Object bean) {
        final Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);
        SmartSpringLifecycle smartSpringLifecycle = beanClass.getAnnotation(SmartSpringLifecycle.class);
        return smartSpringLifecycle != null;
    }
}
