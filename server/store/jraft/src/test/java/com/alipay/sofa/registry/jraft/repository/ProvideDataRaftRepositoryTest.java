package com.alipay.sofa.registry.jraft.repository;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.jraft.AbstractRaftTestBase;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : xingpeng
 * @date : 2021-07-30 08:37
 **/
public class ProvideDataRaftRepositoryTest extends AbstractRaftTestBase {

    @Autowired
    private ProvideDataRepository provideDataRaftRepository;

    @Test
    public void testPut() {
        long version = System.currentTimeMillis();

        String dataInfoId = DataInfo.toDataInfoId("key" + version, "DEFAULT", "DEFAULT");
        PersistenceData persistenceData =
                PersistenceDataBuilder.createPersistenceData(dataInfoId, "val");
        boolean success = provideDataRaftRepository.put(persistenceData, persistenceData.getVersion());
        Assert.assertTrue(success);
        Assert.assertEquals("val", provideDataRaftRepository.get(dataInfoId).getData());
        Assert.assertEquals(
                persistenceData.getVersion(), provideDataRaftRepository.get(dataInfoId).getVersion());
    }

    @Test
    public void testRemove() {
        long version = System.currentTimeMillis();

        String dataInfoId = DataInfo.toDataInfoId("key" + version, "DEFAULT", "DEFAULT");
        PersistenceData persistenceData =
                PersistenceDataBuilder.createPersistenceData(dataInfoId, "val");

        boolean success = provideDataRaftRepository.put(persistenceData, version);
        Assert.assertTrue(success);
        Assert.assertEquals("val", provideDataRaftRepository.get(dataInfoId).getData());
        Assert.assertEquals(
                persistenceData.getVersion(), provideDataRaftRepository.get(dataInfoId).getVersion());
        boolean remove = provideDataRaftRepository.remove(dataInfoId, persistenceData.getVersion());

        Assert.assertTrue(remove);
        Assert.assertTrue(provideDataRaftRepository.get(dataInfoId) == null);
    }

    @Test
    public void testGetAll() {
        long version = System.currentTimeMillis();

        String dataInfoId = DataInfo.toDataInfoId("testGetAll" + version, "DEFAULT", "DEFAULT");
        PersistenceData persistenceData =
                PersistenceDataBuilder.createPersistenceData(dataInfoId, "val");
        boolean success = provideDataRaftRepository.put(persistenceData, persistenceData.getVersion());
        Assert.assertTrue(success);
        Assert.assertEquals("val", provideDataRaftRepository.get(dataInfoId).getData());
        Assert.assertEquals(
                persistenceData.getVersion(), provideDataRaftRepository.get(dataInfoId).getVersion());

        Collection<PersistenceData> all = provideDataRaftRepository.getAll();
        Assert.assertTrue(all.contains(persistenceData));
    }
}
