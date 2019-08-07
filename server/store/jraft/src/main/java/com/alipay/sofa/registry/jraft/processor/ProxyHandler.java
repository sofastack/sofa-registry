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
package com.alipay.sofa.registry.jraft.processor;

import com.alipay.sofa.registry.jraft.bootstrap.RaftClient;
import com.alipay.sofa.registry.jraft.command.ProcessRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author shangyu.wh
 * @version $Id: ProxyHandler.java, v 0.1 2018-05-23 12:19 shangyu.wh Exp $
 */
public class ProxyHandler implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyHandler.class);

    private final Class<?>      interfaceType;

    private final String        serviceId;

    private final RaftClient    client;

    /**
     * constructor
     * @param interfaceType
     * @param serviceId
     * @param client
     */
    public ProxyHandler(Class<?> interfaceType, String serviceId, RaftClient client) {
        this.interfaceType = interfaceType;
        this.serviceId = serviceId;
        this.client = client;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {

            ProcessRequest request = new ProcessRequest();
            request.setMethodArgSigs(createParamSignature(method.getParameterTypes()));
            request.setMethodName(method.getName());
            request.setMethodArgs(args);

            request.setServiceName(serviceId);

            if (Processor.getInstance().isLeaderReadMethod(method)) {
                return doInvokeMethod(request);
            }
            return client.sendRequest(request);
        } catch (Throwable e) {
            LOGGER.error("Proxy invoke interface {} method {} got error!", interfaceType.getName(),
                method.getName(), e);
            throw new RuntimeException(String.format(
                "Proxy invoke interface %s method %s got error!", interfaceType.getName(),
                method.getName()), e);
        }
    }

    private Object doInvokeMethod(ProcessRequest request) {
        try {

            Object target = Processor.getInstance().getWorkers().get(serviceId);
            if (target == null) {
                LOGGER.error("Can not find service {} from process!", serviceId);
                throw new RuntimeException(String.format("Can not find service %s from process!",
                    serviceId));
            }

            Method method = Processor.getInstance().getWorkMethod(request);

            MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);
            return methodHandle.bindTo(target).invokeWithArguments(request.getMethodArgs());
        } catch (Throwable e) {
            LOGGER.error("Directly invoke read only service {} method {} error!",
                request.getServiceName(), request.getMethodName(), e);
            throw new RuntimeException(String.format(
                "Directly invoke read only service %s method %s error!", request.getServiceName(),
                request.getMethodName()), e);
        }
    }

    private String[] createParamSignature(Class<?>[] args) {
        if (args == null || args.length == 0) {
            return new String[] {};
        }
        String[] paramSig = new String[args.length];
        for (int x = 0; x < args.length; x++) {
            paramSig[x] = args[x].getName();
        }
        return paramSig;
    }

}