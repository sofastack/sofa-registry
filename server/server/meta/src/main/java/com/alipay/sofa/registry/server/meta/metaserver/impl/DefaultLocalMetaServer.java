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
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.processor.SnapshotProcess;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.store.api.annotation.RaftService;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.FileUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chen.zhu
 * <p>
 * Dec 14, 2020
 */
@RaftService(uniqueId = DefaultLocalMetaServer.DEFAULT_LOCAL_META_SERVER, interfaceType = CurrentDcMetaServer.class)
public class DefaultLocalMetaServer extends AbstractMetaServer implements CurrentDcMetaServer,
                                                              SnapshotProcess {

    public static final String DEFAULT_LOCAL_META_SERVER = "DefaultLocalMetaServer";

    private final AtomicLong   currentEpoch              = new AtomicLong();

    @Autowired
    private SlotManager        slotManager;

    @Override
    public SlotTable getSlotTable() {
        return slotManager.getSlotTable();
    }

    public long getEpoch() {
        return currentEpoch.get();
    }

    @Override
    public List<MetaNode> getClusterMembers() {
        return Lists.newArrayList(metaServers);
    }

    @Override
    public void updateClusterMembers(List<MetaNode> newMembers, long epoch) {
        if (epoch <= currentEpoch.get()) {
            logger.warn("[updateClusterMembers]Epoch[{}] is less than current[{}], ignore: {}",
                currentEpoch.get(), epoch, newMembers);
        }
        lock.writeLock().lock();
        try {
            logger.warn(
                "[updateClusterMembers] update meta-servers, \nprevious[{}]: {} \ncurrent[{}]: {}",
                currentEpoch.get(), getClusterMembers(), epoch, newMembers);
            currentEpoch.set(epoch);
            metaServers = Sets.newHashSet(newMembers);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void renew(MetaNode metaNode) {
        lock.writeLock().lock();
        try {
            if (logger.isInfoEnabled()) {
                logger.info("[renew]meta node [{}] renewed", metaNode);
            }
            metaServers.add(metaNode);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void cancel(MetaNode renewal) {
        lock.writeLock().lock();
        try {
            if (logger.isInfoEnabled()) {
                logger.info("[cancel]meta node [{}] removed", renewal);
            }
            metaServers.remove(renewal);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean save(String path) {
        if (logger.isInfoEnabled()) {
            logger.info("[save] start to save {}", path);
        }
        return save(path, metaServers);
    }

    @Override
    public boolean load(String path) {
        if (logger.isInfoEnabled()) {
            logger.info("[load] start to load {}", path);
        }
        try {
            Set<MetaNode> metaNodes = load(path, metaServers.getClass());
            metaServers.clear();
            metaServers.addAll(metaNodes);
            currentEpoch.set(DatumVersionUtil.nextId());
            return true;
        } catch (IOException e) {
            logger.error("Load meta servers data error!", e);
            return false;
        }
    }

    @Override
    public SnapshotProcess copy() {
        DefaultLocalMetaServer metaServer = new DefaultLocalMetaServer();
        metaServer.updateClusterMembers(this.getClusterMembers(), this.getEpoch());
        return metaServer;
    }

    @Override
    public Set<String> getSnapshotFileNames() {
        Set<String> set = Sets.newHashSet();
        set.add("DefaultLocalMetaServer");
        return set;
    }

    /**
     * save snapshot to file
     * @param path
     * @param values
     * @return
     */
    public boolean save(String path, Object values) {
        try {
            FileUtils.writeByteArrayToFile(new File(path), CommandCodec.encodeCommand(values),
                false);
            return true;
        } catch (IOException e) {
            logger.error("Fail to save snapshot", e);
            return false;
        }
    }

    /**
     * load snapshot from file
     * @param path
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> T load(String path, Class<T> clazz) throws IOException {
        byte[] bs = FileUtils.readFileToByteArray(new File(path));
        if (bs.length > 0) {
            return CommandCodec.decodeCommand(bs, clazz);
        }
        throw new IOException("Fail to load snapshot from " + path + ", content: "
                              + Arrays.toString(bs));
    }

    @VisibleForTesting
    protected DefaultLocalMetaServer setSlotManager(SlotManager slotManager) {
        this.slotManager = slotManager;
        return this;
    }
}
