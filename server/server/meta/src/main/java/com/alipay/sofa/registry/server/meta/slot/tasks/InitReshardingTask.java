package com.alipay.sofa.registry.server.meta.slot.tasks;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Dec 01, 2020
 */
public class InitReshardingTask implements RebalanceTask {

    private final LocalSlotManager localSlotManager;

    private final SlotManager raftSlotManager;

    private final DataServerManager dataServerManager;

    private final Random random = new Random();

    private long nextEpoch;

    public InitReshardingTask(LocalSlotManager localSlotManager, SlotManager raftSlotManager,
                              DataServerManager dataServerManager) {
        this.localSlotManager = localSlotManager;
        this.raftSlotManager = raftSlotManager;
        this.dataServerManager = dataServerManager;
    }


    @Override
    public void run() {
        List<DataNode> dataNodes = dataServerManager.getClusterMembers();
        List<String> candidates = Lists.newArrayListWithCapacity(dataNodes.size());
        dataNodes.forEach(dataNode -> candidates.add(dataNode.getIp()));
        Map<Integer, Slot> slotMap = Maps.newHashMap();
        for(int slotId = 0; slotId < localSlotManager.getSlotNums(); slotId++) {
            Set<String> selected = Sets.newHashSet();
            nextEpoch = DatumVersionUtil.nextId();
            String leader = randomSelect(candidates, selected);
            List<String> followers = Lists.newArrayListWithCapacity(localSlotManager.getSlotReplicaNums());
            for(int replica = 0; replica < localSlotManager.getSlotReplicaNums(); replica++) {
                followers.add(randomSelect(candidates, selected));
            }
            slotMap.put(slotId, new Slot(slotId, leader, nextEpoch, followers));
        }
        raftSlotManager.refresh(new SlotTable(nextEpoch, slotMap));
    }

    private String randomSelect(List<String> candidates, Set<String> selected) {
        int randomIndex = Math.abs(random.nextInt()) & candidates.size();
        String result = candidates.get(randomIndex);
        while(selected.contains(result)) {
            randomIndex = Math.abs(random.nextInt()) & candidates.size();
            result = candidates.get(randomIndex);
        }
        return result;
    }
}
