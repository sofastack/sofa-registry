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

import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.jraft.command.ProcessRequest;
import com.alipay.sofa.registry.jraft.command.ProcessResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.annotation.NonRaftMethod;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author shangyu.wh
 * @version $Id: Processor.java, v 0.1 2018-05-17 19:05 shangyu.wh Exp $
 */
public class Processor {

    private static final Logger              LOG                  = LoggerFactory
                                                                      .getLogger(Processor.class);

    private Map<String, Map<String, Method>> workerMethods        = new HashMap<>();

    private Map<String, Object>              workers              = new HashMap<>();

    private Map<String, MethodHandle>        methodHandleMap      = new ConcurrentHashMap<>();

    private final static String              SERVICE_METHOD_SPLIT = "#@#";

    private static volatile Processor        instance;

    /**
     * get processor instance
     * @return
     */
    public static Processor getInstance() {
        Processor processor = instance;
        if (processor == null) {
            synchronized (Processor.class) {
                processor = instance;
                if (processor == null) {
                    instance = new Processor();
                }
            }
        }
        return instance;
    }

    public void addWorker(String serviceId, Class interfaceClazz, Object target) {
        if (workers.get(serviceId) != null) {
            LOG.warn("Service {} has bean existed!", serviceId);
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("[addWorker] serviceId: {}, interfaceClazz: {}, target: {}", serviceId,
                interfaceClazz, target);
        }
        Map<String, Method> publicMethods = new HashMap();
        for (Method m : interfaceClazz.getMethods()) {
            publicMethods.put(getSignificantMethodName(m), m);
        }

        workerMethods.put(serviceId, publicMethods);
        workers.put(serviceId, target);
    }

    public static String getSignificantMethodName(Method method) {
        StringBuilder mSigs = new StringBuilder();
        mSigs.append(method.getName());
        for (Class<?> paramType : method.getParameterTypes()) {
            mSigs.append(paramType.getName());
        }
        return mSigs.toString();
    }

    public ProcessResponse process(ProcessRequest request) {
        String methodName = request.getMethodName();
        String serviceId = request.getServiceName();
        Object target = workers.get(serviceId);
        if (target == null) {
            LOG.warn("Can not find service {} from process!", serviceId);
            return ProcessResponse.fail(
                String.format("Can not find service %s from process!", serviceId)).build();
        }

        try {
            StringBuilder methodKeyBuffer = new StringBuilder();
            methodKeyBuffer.append(methodName);
            String[] sig = request.getMethodArgSigs();
            for (int i = 0; i < sig.length; i++) {
                methodKeyBuffer.append(sig[i]);
            }
            String methodKey = methodKeyBuffer.toString();
            Method appServiceMethod = workerMethods.get(serviceId).get(methodKey);
            if (appServiceMethod == null) {
                LOG.error("Can not find method {} from processor by serviceId {}", methodName,
                    serviceId);
                throw new NoSuchMethodException("Can not find method from processor！");
            }
            Object[] methodArg = request.getMethodArgs();
            String methodHandleKey = getMethodHandleKey(serviceId, methodKey);

            MethodHandle methodHandle = methodHandleMap.computeIfAbsent(methodHandleKey,k-> {
                try {
                    return MethodHandles.lookup().unreflect(appServiceMethod);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Process service request lookup method error!",e);
                }
            });

            Object ret = methodHandle.bindTo(target).invokeWithArguments(methodArg);
            if (ret != null) {
                return ProcessResponse.ok(ret).build();
            } else {
                return ProcessResponse.ok().build();
            }

        } catch (Throwable e) {
            LOG.error("Process service request {} error!", request, e);
            return ProcessResponse.fail(
                String.format("Process service %s method %s error!", serviceId, methodName))
                .build();
        }
    }

    public ProcessResponse process(Method method, ProcessRequest request) {
        String methodName = request.getMethodName();
        String serviceId = request.getServiceName();
        Object target = workers.get(serviceId);
        if (target == null) {
            LOG.warn("Can not find service {} from process!", serviceId);
            return ProcessResponse.fail(
                String.format("Can not find service %s from process!", serviceId)).build();
        }

        try {
            Object[] methodArg = request.getMethodArgs();

            StringBuilder methodKeyBuffer = new StringBuilder();
            methodKeyBuffer.append(methodName);
            String[] sig = request.getMethodArgSigs();
            for (int i = 0; i < sig.length; i++) {
                methodKeyBuffer.append(sig[i]);
            }
            String methodKey = methodKeyBuffer.toString();
            String methodHandleKey = getMethodHandleKey(serviceId, methodKey);
            MethodHandle methodHandle = methodHandleMap.computeIfAbsent(methodHandleKey,k-> {
                try {
                    return MethodHandles.lookup().unreflect(method);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Process service request lookup method error!",e);
                }
            });

            Object ret = methodHandle.bindTo(target).invokeWithArguments(methodArg);
            if (ret != null) {
                return ProcessResponse.ok(ret).build();
            } else {
                return ProcessResponse.ok().build();
            }

        } catch (Throwable e) {
            LOG.error("Process service request {} error!", request, e);
            return ProcessResponse.fail(
                String.format("Process service %s method %s error!", serviceId, methodName))
                .build();
        }
    }

    public Method getWorkMethod(ProcessRequest request) {
        String methodName = request.getMethodName();
        String serviceId = request.getServiceName();
        try {

            StringBuilder methodKeyBuffer = new StringBuilder();
            methodKeyBuffer.append(methodName);
            String[] sig = request.getMethodArgSigs();
            for (int i = 0; i < sig.length; i++) {
                methodKeyBuffer.append(sig[i]);
            }
            Method appServiceMethod = workerMethods.get(serviceId).get(methodKeyBuffer.toString());
            if (appServiceMethod == null) {
                LOG.error("Can not find method {} from processor by serviceId {}", methodName,
                    serviceId);
                throw new NoSuchMethodException("Can not find method from processor！");
            }
            return appServiceMethod;
        } catch (Throwable e) {
            LOG.error("Process request {} get WorkMethod error!", request, e);
            throw new RuntimeException(String.format("Process request %s get WorkMethod error!",
                request, e));
        }
    }

    public MethodHandle getWorkMethodHandle(ProcessRequest request) {
        String methodName = request.getMethodName();
        String serviceId = request.getServiceName();
        try {

            StringBuilder methodKeyBuffer = new StringBuilder();
            methodKeyBuffer.append(methodName);
            String[] sig = request.getMethodArgSigs();
            for (int i = 0; i < sig.length; i++) {
                methodKeyBuffer.append(sig[i]);
            }
            String methodKey = methodKeyBuffer.toString();
            Method appServiceMethod = workerMethods.get(serviceId).get(methodKey);
            if (appServiceMethod == null) {
                LOG.error("Can not find method {} from processor by serviceId {}", methodName,
                        serviceId);
                throw new NoSuchMethodException("Can not find method from processor！");
            }

            String methodHandleKey = getMethodHandleKey(serviceId, methodKey);

            MethodHandle methodHandle = methodHandleMap.computeIfAbsent(methodHandleKey,k-> {
                try {
                    return MethodHandles.lookup().unreflect(appServiceMethod);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Process service request lookup method error!",e);
                }
            });
            return methodHandle;
        } catch (Throwable e) {
            LOG.error("Process request {} get WorkMethod error!", request, e);
            throw new RuntimeException(String.format("Process request %s get WorkMethod error!",
                    request, e));
        }
    }

    public Map<String, Object> getWorkers() {
        return workers;
    }

    public boolean isLeaderDirectExecuteMethod(Method method) {
        if (ServiceStateMachine.getInstance().isLeader()) {
            return method != null
                   && (method.isAnnotationPresent(ReadOnLeader.class) || method
                       .isAnnotationPresent(NonRaftMethod.class));
        }
        return false;
    }

    public String getMethodHandleKey(String serviceId, String methodKey) {
        return serviceId + SERVICE_METHOD_SPLIT + methodKey;
    }

}
