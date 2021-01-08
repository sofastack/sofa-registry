package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.impl.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import com.alipay.sofa.registry.server.meta.slot.tasks.InitReshardingTask;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author chen.zhu
 * <p>
 * Jan 08, 2021
 */
@Path("openapi/slot/table")
public class SlotTableResource {

    @Autowired
    private DefaultSlotManager defaultSlotManager;

    @Autowired
    private LocalSlotManager slotManager;

    @Autowired
    private DefaultDataServerManager dataServerManager;

    @GET
    @Path("force/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public SlotTable forceRefreshSlotTable() {
        InitReshardingTask task = new InitReshardingTask(slotManager,
                defaultSlotManager.getRaftSlotManager(), dataServerManager);
        task.run();
        return slotManager.getSlotTable();
    }

    public SlotTableResource() {
    }

    public SlotTableResource(DefaultSlotManager defaultSlotManager, LocalSlotManager slotManager,
                             DefaultDataServerManager dataServerManager) {
        this.defaultSlotManager = defaultSlotManager;
        this.slotManager = slotManager;
        this.dataServerManager = dataServerManager;
    }
}
