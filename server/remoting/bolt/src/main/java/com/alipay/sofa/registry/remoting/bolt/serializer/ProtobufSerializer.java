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
package com.alipay.sofa.registry.remoting.bolt.serializer;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.serialization.Serializer;
import com.google.protobuf.MessageLite;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhanggeng on 2017/2/23.
 * @author zhanggeng
 * @version $Id : ProtobufSerializer.java, v 0.1 2018-05-05 23:43 zhanggeng Exp $$
 */
public class ProtobufSerializer implements Serializer {

    public static final byte                 PROTOCOL_PROTOBUF    = 11;

    /** cache parse method */
    private ConcurrentHashMap<Class, Method> parseMethodMap       = new ConcurrentHashMap<>();

    /** cache toByteArray method */
    private ConcurrentHashMap<Class, Method> toByteArrayMethodMap = new ConcurrentHashMap<>();

    private static ProtobufSerializer        instance             = new ProtobufSerializer();

    private ProtobufSerializer() {

    }

    public static ProtobufSerializer getInstance() {
        return instance;
    }

    @Override
    public byte[] serialize(Object object) throws CodecException {
        if (object == null) {
            throw new SerializationException("Unsupported null message");
        } else if (isProtoBufMessageLite(object)) {
            Class clazz = object.getClass();
            Method method = toByteArrayMethodMap.get(clazz);
            if (method == null) {
                try {
                    method = clazz.getMethod("toByteArray");
                    method.setAccessible(true);
                    toByteArrayMethodMap.put(clazz, method);
                } catch (Exception e) {
                    throw new SerializationException(
                        "Cannot found method " + clazz.getName()
                                + ".toByteArray(), please check the generated code");
                }
            }
            try {
                return (byte[]) method.invoke(object);
            } catch (Exception e) {
                throw new SerializationException(
                    "Cannot found method " + clazz.getName()
                            + ".toByteArray(), please check the generated code");
            }

        } else if (object instanceof String) {
            try {
                // return ByteString.copyFromUtf8((String) object).toByteArray();
                return ((String) object).getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new SerializationException("Unsupported encoding of string", e);
            }
        } else {
            throw new SerializationException("Unsupported class:" + object.getClass().getName()
                                             + ", only support protobuf message");
        }
    }

    @Override
    public <T> T deserialize(byte[] data, String classOfT) throws CodecException {
        try {
            Class requestClass = Class.forName(classOfT);
            return (T) decode(data, requestClass);
        } catch (ClassNotFoundException e) {
            throw new SerializationException("Cannot found class " + classOfT, e);
        }
    }

    public Object decode(byte[] bytes, Class clazz) throws DeserializationException {
        if (isProtoBufMessageLite(clazz)) {
            try {
                Method method = parseMethodMap.get(clazz);
                if (method == null) {
                    method = clazz.getMethod("parseFrom", byte[].class);
                    if (!Modifier.isStatic(method.getModifiers())) {
                        throw new CodecException(
                            "Cannot found method " + clazz.getName()
                                    + ".parseFrom(byte[]), please check the generated code");
                    }
                    method.setAccessible(true);
                    parseMethodMap.put(clazz, method);
                }
                return method.invoke(null, bytes);
            } catch (DeserializationException e) {
                throw e;
            } catch (Exception e) {
                throw new DeserializationException(
                    "Cannot found method " + clazz.getName()
                            + ".parseFrom(byte[]), please check the generated code", e);
            }
        } else if (clazz == String.class) {
            try {
                // return ByteString.wrap(bytes).toStringUtf8();
                return new String(bytes, "utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new DeserializationException("Unsupported encoding of string", e);
            }
        } else {
            throw new DeserializationException("Unsupported class:" + clazz.getName()
                                               + ", only support protobuf message");
        }
    }

    /**
     * 请求参数类型缓存 {service+method:class}
     */
    private static ConcurrentHashMap<String, Class> REQUEST_CLASS_CACHE  = new ConcurrentHashMap<>();

    /**
     * 返回结果类型缓存 {service+method:class}
     */
    private static ConcurrentHashMap<String, Class> RESPONSE_CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * 从缓存中获取请求值类
     *
     * @param service    接口名
     * @param methodName 方法名
     * @return 请求参数类
     * @throws ClassNotFoundException 无法找到该接口
     * @throws NoSuchMethodException  无法找到该方法
     * @throws CodecException         其它序列化异常
     */
    public static Class getReqClass(String service, String methodName, ClassLoader classLoader)
                                                                                               throws ClassNotFoundException,
                                                                                               NoSuchMethodException,
                                                                                               CodecException {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        String key = buildMethodKey(service, methodName);
        Class reqClass = REQUEST_CLASS_CACHE.get(key);
        if (reqClass == null) {
            // 读取接口里的方法参数和返回值
            String interfaceClass = service.contains(":") ? service.substring(0,
                service.indexOf(":")) : service;
            Class clazz = Class.forName(interfaceClass, true, classLoader);
            loadProtoClassToCache(key, clazz, methodName);
        }
        return REQUEST_CLASS_CACHE.get(key);
    }

    /**
     * 从缓存中获取返回值类
     *
     * @param service    接口名
     * @param methodName 方法名
     * @return 请求参数类
     * @throws ClassNotFoundException 无法找到该接口
     * @throws NoSuchMethodException  无法找到该方法
     * @throws CodecException         其它序列化异常
     */
    public static Class getResClass(String service, String methodName, ClassLoader classLoader)
                                                                                               throws ClassNotFoundException,
                                                                                               NoSuchMethodException,
                                                                                               CodecException {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        String key = service + "#" + methodName;
        Class reqClass = RESPONSE_CLASS_CACHE.get(key);
        if (reqClass == null) {
            // 读取接口里的方法参数和返回值
            String interfaceClass = service.contains(":") ? service.substring(0,
                service.indexOf(":")) : service;
            Class clazz = Class.forName(interfaceClass, true, classLoader);
            loadProtoClassToCache(key, clazz, methodName);
        }
        return RESPONSE_CLASS_CACHE.get(key);
    }

    /**
     * 拼装缓存的key
     *
     * @param serviceName 接口名
     * @param methodName  方法名
     * @return
     */
    private static String buildMethodKey(String serviceName, String methodName) {
        return serviceName + "#" + methodName;
    }

    /**
     * 加载protobuf接口里方法的参数和返回值类型到缓存，不需要传递
     *
     * @param key        缓存的key
     * @param clazz      接口名
     * @param methodName 方法名
     * @throws ClassNotFoundException 无法找到该接口
     * @throws NoSuchMethodException  无法找到该方法
     * @throws CodecException         其它序列化异常
     */
    private static void loadProtoClassToCache(String key, Class clazz, String methodName)
                                                                                         throws CodecException {
        Method pbMethod = null;
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                pbMethod = method;
                break;
            }
        }
        if (pbMethod != null) {
            Class[] parameterTypes = pbMethod.getParameterTypes();
            if (parameterTypes.length != 1 || !isProtoBufMessageLite(parameterTypes[0])) {
                throw new CodecException("class based protobuf: " + clazz.getName()
                                         + ", only support one protobuf parameter!");
            }
            Class reqClass = parameterTypes[0];
            REQUEST_CLASS_CACHE.put(key, reqClass);
            Class resClass = pbMethod.getReturnType();
            if (resClass == void.class || !isProtoBufMessageLite(resClass)) {
                throw new CodecException("class based protobuf: " + clazz.getName()
                                         + ", only support return protobuf message!");
            }
            RESPONSE_CLASS_CACHE.put(key, resClass);
        }
    }

    public static boolean isProtoBufMessageLite(Object object) {
        Class clzz = object == null ? null : object.getClass();
        return isProtoBufMessageLite(clzz);
    }

    private static boolean isProtoBufMessageLite(Class clzz) {
        if (clzz != null) {
            return isSpecificationInterface(clzz, MessageLite.class.getCanonicalName());
        }
        return false;
    }

    /**
     * whether  clazz has a interface named interfaceName
     *
     * @param clazz
     * @param interfaceName
     * @return
     */
    private static boolean isSpecificationInterface(Class<?> clazz, String interfaceName) {

        boolean find = false;

        // First, get all direct interface
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            for (Class<?> interfaze : interfaces) {
                find = interfaceName.equalsIgnoreCase(interfaze.getCanonicalName());
                if (find) {
                    break;
                }
            }
        }
        while (!Object.class.getCanonicalName().equals(clazz.getCanonicalName()) && !find) {
            // Add the super class
            Class<?> superClass = clazz.getSuperclass();

            // Interfaces does not have java,lang.Object as superclass, they have null, so break the cycle and return
            if (superClass == null) {
                break;
            }

            // Now inspect the superclass
            clazz = superClass;

            find = isSpecificationInterface(clazz, interfaceName);

        }

        return find;
    }
}
