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
package com.alipay.sofa.registry.server.meta.metaserver.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.MetaServer;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public abstract class AbstractMetaServer extends AbstractLifecycleObservable implements MetaServer {

    protected List<MetaNode>      metaServers = Lists.newCopyOnWriteArrayList();

    protected final ReadWriteLock lock        = new ReentrantReadWriteLock();

    @ReadOnLeader
    public List<MetaNode> getClusterMembers() {
        lock.readLock().lock();
        try {
            return Lists.newArrayList(metaServers);
        } finally {
            lock.readLock().unlock();
        }
    }

}
