/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class SmartSpringLifecycleController implements BeanPostProcessor,
                                           DestructionAwareBeanPostProcessor, Ordered {

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (!isSmartSpringLifecycleBean(bean)) {
            return;
        }
        try {
            LifecycleHelper.stopIfPossible(bean);
            LifecycleHelper.disposeIfPossible(bean);
        } catch (Exception e) {
            throw new SofaRegistryRuntimeException(String.format("[%s]%s", beanName,
                "Lifecycle dispose exception"), e);
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return true;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
                                                                               throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
                                                                              throws BeansException {
        if (!isSmartSpringLifecycleBean(bean)) {
            return bean;
        }
        try {
            LifecycleHelper.initializeIfPossible(bean);
            LifecycleHelper.startIfPossible(bean);
        } catch (Exception e) {
            throw new SofaRegistryRuntimeException(String.format("[%s]%s", beanName,
                "Lifecycle init exception"), e);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private boolean isSmartSpringLifecycleBean(Object bean) {
        final Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);
        SmartSpringLifecycle smartSpringLifecycle = beanClass
            .getAnnotation(SmartSpringLifecycle.class);
        return smartSpringLifecycle != null;
    }
}
