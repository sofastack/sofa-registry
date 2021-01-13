package com.alipay.sofa.registry.server.shared.resource;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
@Path("openapi/v1/slot")
public class SlotGenericResource implements SlotTableRecorder {

    private AtomicReference<SlotTable> slotTableRef = new AtomicReference<>(SlotTable.INIT);

    @GET
    @Path("/table")
    @Produces(MediaType.APPLICATION_JSON)
    public GenericResponse<SlotTable> slotTable() {
        SlotTable slotTable = slotTableRef.get();
        return new GenericResponse<SlotTable>().fillSucceed(slotTable);
    }

    @GET
    @Path("/epoch")
    @Produces(MediaType.APPLICATION_JSON)
    public GenericResponse<Long> epoch() {
        Long epoch = slotTableRef.get().getEpoch();
        return new GenericResponse<Long>().fillSucceed(epoch);
    }

    @Override
    public void record(SlotTable slotTable) {
        this.slotTableRef.set(slotTable);
    }
}
