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

import com.alipay.sofa.registry.jraft.bootstrap.RaftClient;
import com.alipay.sofa.registry.jraft.processor.Processor;
import com.alipay.sofa.registry.jraft.processor.ProxyHandler;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.store.api.annotation.RaftReference;
import com.alipay.sofa.registry.store.api.annotation.RaftService;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

/**
 *
 * @author shangyu.wh
 * @version $Id: AnnotationBeanPostProcessor.java, v 0.1 2018-05-22 22:44 shangyu.wh Exp $
 */
public class RaftAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(RaftAnnotationBeanPostProcessor.class);

    @Autowired
    private RaftExchanger       raftExchanger;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
                                                                               throws BeansException {
        processRaftReference(bean);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
                                                                              throws BeansException {
        processRaftService(bean, beanName);
        return bean;
    }

    private void processRaftReference(Object bean) {
        final Class<?> beanClass = bean.getClass();

        ReflectionUtils.doWithFields(beanClass, field -> {
            RaftReference referenceAnnotation = field.getAnnotation(RaftReference.class);

            if (referenceAnnotation == null) {
                return;
            }

            Class<?> interfaceType = referenceAnnotation.interfaceType();

            if (interfaceType.equals(void.class)) {
                interfaceType = field.getType();
            }
            String serviceId = getServiceId(interfaceType, referenceAnnotation.uniqueId());
            Object proxy = getProxy(interfaceType, serviceId);
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, proxy);

        }, field -> !Modifier.isStatic(field.getModifiers())
                && field.isAnnotationPresent(RaftReference.class));
    }

    private Object getProxy(Class<?> interfaceType, String serviceId) {
        RaftClient client = raftExchanger.getRaftClient();
        if (client == null) {
            raftExchanger.startRaftClient();
            LOGGER.info("Raft client before started!");
        }
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
            new Class<?>[] { interfaceType }, new ProxyHandler(interfaceType, serviceId,
                raftExchanger.getRaftClient()));
    }

    private void processRaftService(Object bean, String beanName) {

        final Class<?> beanClass = AopProxyUtils.ultimateTargetClass(bean);

        RaftService raftServiceAnnotation = beanClass.getAnnotation(RaftService.class);

        if (raftServiceAnnotation == null) {
            return;
        }

        Class<?> interfaceType = raftServiceAnnotation.interfaceType();

        if (interfaceType.equals(void.class)) {
            Class<?>[] interfaces = beanClass.getInterfaces();

            if (interfaces == null || interfaces.length == 0 || interfaces.length > 1) {
                throw new RuntimeException(
                    "Bean " + beanName
                            + " does not has any interface or has more than one interface.");
            }

            interfaceType = interfaces[0];
        }
        String serviceUniqueId = getServiceId(interfaceType, raftServiceAnnotation.uniqueId());
        Processor.getInstance().addWorker(serviceUniqueId, interfaceType, bean);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private String getServiceId(Class<?> interfaceType, String uniqueId) {
        if (interfaceType == null) {
            throw new IllegalArgumentException("Get serviceId error!interfaceType can not be null!");
        }
        if (uniqueId != null && !uniqueId.isEmpty()) {
            return interfaceType.getName() + ":" + uniqueId;
        } else {
            return interfaceType.getName();
        }
    }
}