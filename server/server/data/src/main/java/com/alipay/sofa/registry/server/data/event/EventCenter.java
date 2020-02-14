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
package com.alipay.sofa.registry.server.data.event;

import java.util.Collection;
import java.util.List;

import com.alipay.sofa.registry.server.data.event.handler.AbstractEventHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 *
 * @author qian.lqlq
 * @version $Id: EventCenter.java, v 0.1 2018-03-13 14:41 qian.lqlq Exp $
 */
public class EventCenter {

    private Multimap<Class<? extends Event>, AbstractEventHandler> MAP = ArrayListMultimap.create();

    /**
     * eventHandler register
     * @param handler
     */
    public void register(AbstractEventHandler handler) {
        List<Class<? extends Event>> interests = handler.interest();
        for (Class<? extends Event> interest : interests) {
            MAP.put(interest, handler);
        }
    }

    /**
     * event handler handle process
     * @param event
     */
    public void post(Event event) {
        Class clazz = event.getClass();
        if (MAP.containsKey(clazz)) {
            Collection<AbstractEventHandler> handlers = MAP.get(clazz);
            if (handlers != null) {
                for (AbstractEventHandler handler : handlers) {
                    handler.handle(event);
                }
            }
        } else {
            throw new RuntimeException("no suitable handler was found:" + clazz);
        }
    }
}