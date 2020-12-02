package com.alipay.sofa.registry.server.meta.slot.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.jraft.processor.Processor;
import com.alipay.sofa.registry.jraft.processor.ProxyHandler;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Proxy;

/**
 * @author chen.zhu
 * <p>
 * Dec 02, 2020
 */
public class DefaultSlotManager extends AbstractLifecycle implements SlotManager {

    @Autowired
    private RaftExchanger raftExchanger;

    @Autowired
    private LocalSlotManager localSlotManager;

    private SlotManager raftSlotManager;

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        initRaftService();
    }

    @Override
    protected void doStart() throws StartException {
        super.doStart();
    }

    @Override
    protected void doStop() throws StopException {
        super.doStop();
    }

    @Override
    protected void doDispose() throws DisposeException {
        super.doDispose();
    }

    @Override
    public void refresh(SlotTable slotTable) {
        raftSlotManager.refresh(slotTable);
    }

    @Override
    public int getSlotNums() {
        return raftSlotManager.getSlotNums();
    }

    @Override
    public int getSlotReplicaNums() {
        return raftSlotManager.getSlotReplicaNums();
    }

    @Override
    public DataNodeSlot getDataNodeManagedSlot(DataNode dataNode, boolean ignoreFollowers) {
        return raftSlotManager.getDataNodeManagedSlot(dataNode, ignoreFollowers);
    }

    @Override
    public SlotTable getSlotTable() {
        return raftSlotManager.getSlotTable();
    }

    private String getServiceId() {
        return "DefaultSlotManager.RaftService";
    }

    private void initRaftService() {
        localSlotManager = new LocalSlotManager();
        Processor.getInstance().addWorker(getServiceId(), SlotManager.class, localSlotManager);
        raftSlotManager = (SlotManager) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] {SlotManager.class },
                new ProxyHandler(SlotManager.class, getServiceId(), raftExchanger.getRaftClient()));
    }
}
