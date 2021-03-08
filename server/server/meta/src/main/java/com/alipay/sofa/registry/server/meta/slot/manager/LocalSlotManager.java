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
package com.alipay.sofa.registry.server.meta.slot.manager;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.processor.SnapshotProcess;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.Observer;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.remoting.notifier.Notifier;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.jraft.annotation.RaftService;
import com.alipay.sofa.registry.util.FileUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 * <p>
 * Nov 13, 2020
 */
@RaftService(uniqueId = LocalSlotManager.LOCAL_SLOT_MANAGER, interfaceType = SlotManager.class)
public class LocalSlotManager extends AbstractLifecycleObservable implements SlotManager,
                                                                 SnapshotProcess {

    public static final String               LOCAL_SLOT_MANAGER = "LocalSlotManager";

    @Autowired
    private NodeConfig                       nodeConfig;

    @Autowired
    private List<Notifier>                   notifiers;

    private final ReadWriteLock              lock               = new ReentrantReadWriteLock();

    private final AtomicReference<SlotTable> currentSlotTable   = new AtomicReference<>(
                                                                    SlotTable.INIT);

    private Map<String, DataNodeSlot>        reverseMap         = ImmutableMap.of();

    public LocalSlotManager() {
    }

    public LocalSlotManager(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    @PostConstruct
    public void postConstruct() throws Exception {
        LifecycleHelper.initializeIfPossible(this);
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        addObserver(new SlotTableChangeNotification());
    }

    @Override
    public SlotTable getSlotTable() {
        lock.readLock().lock();
        try {
            return currentSlotTable.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void refresh(SlotTable slotTable) {
        lock.writeLock().lock();
        try {
            if (slotTable.getEpoch() <= currentSlotTable.get().getEpoch()) {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                        "[refresh]receive slot table,but epoch({}) is smaller than current({})",
                        slotTable.getEpoch(), currentSlotTable.get().getEpoch());
                }
                return;
            }
            setSlotTable(slotTable);
            refreshReverseMap(slotTable);
        } finally {
            lock.writeLock().unlock();
        }
        notifyObservers(slotTable);
    }

    private void refreshReverseMap(SlotTable slotTable) {
        Map<String, DataNodeSlot> newMap = Maps.newHashMap();
        List<DataNodeSlot> dataNodeSlots = slotTable.transfer(null, false);
        dataNodeSlots.forEach(dataNodeSlot -> {
            try {
                newMap.put(dataNodeSlot.getDataNode(), dataNodeSlot);
            } catch (Exception e) {
                logger.error("[refreshReverseMap][{}]", dataNodeSlot.getDataNode(), e);
            }
        });
        this.reverseMap = ImmutableMap.copyOf(newMap);
    }

    @Override
    public int getSlotNums() {
        return SlotConfig.SLOT_NUM;
    }

    @Override
    public int getSlotReplicaNums() {
        return SlotConfig.SLOT_REPLICAS;
    }

    @Override
    public DataNodeSlot getDataNodeManagedSlot(DataNode dataNode, boolean ignoreFollowers) {
        lock.readLock().lock();
        try {
            // here we ignore port for data-node, as when store the reverse-map, we lose the port information
            // besides, port is not matter here
            DataNodeSlot target = reverseMap.get(dataNode.getIp());
            if (target == null) {
                return new DataNodeSlot(dataNode.getIp());
            }
            return target.fork(ignoreFollowers);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setSlotTable(SlotTable slotTable) {
        this.currentSlotTable.set(slotTable);
    }

    @Override
    public boolean save(String path) {
        try {
            FileUtils.writeByteArrayToFile(new File(path),
                CommandCodec.encodeCommand(currentSlotTable.get()), false);
            return true;
        } catch (IOException e) {
            logger.error("Fail to save snapshot", e);
            return false;
        }
    }

    @Override
    public boolean load(String path) {
        byte[] bs = new byte[0];
        try {
            bs = FileUtils.readFileToByteArray(new File(path));
        } catch (IOException e) {
            logger.error("[load]", e);
        }
        if (bs.length > 0) {
            SlotTable slotTable = CommandCodec.decodeCommand(bs, SlotTable.class);
            refresh(slotTable);
            return true;
        }
        return false;
    }

    @Override
    public SnapshotProcess copy() {
        LocalSlotManager slotManager = new LocalSlotManager();
        slotManager.setSlotTable(currentSlotTable.get());
        return slotManager;
    }

    @Override
    public Set<String> getSnapshotFileNames() {
        Set<String> files = Sets.newHashSet();
        files.add(getClass().getSimpleName());
        return files;
    }

    private final class SlotTableChangeNotification implements Observer {

        @Override
        public void update(Observable source, Object message) {
            if (message instanceof SlotTable) {
                if (notifiers == null || notifiers.isEmpty()) {
                    return;
                }
                notifiers.forEach(notifier -> {
                    try {
                        notifier.notifySlotTableChange((SlotTable) message);
                    } catch (Throwable th) {
                        logger.error("[notify] notifier [{}]", notifier.getClass().getSimpleName(), th);
                    }
                });
            }
        }
    }

    @VisibleForTesting
    public LocalSlotManager setNotifiers(List<Notifier> notifiers) {
        this.notifiers = notifiers;
        return this;
    }
}
