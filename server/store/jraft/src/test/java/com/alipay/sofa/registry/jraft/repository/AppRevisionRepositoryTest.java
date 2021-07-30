package com.alipay.sofa.registry.jraft.repository;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.jraft.AbstractRaftTestBase;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jraft.repository.impl.AppRevisionHeartbeatRaftRepository;
import com.alipay.sofa.registry.jraft.repository.impl.AppRevisionRaftRepository;
import com.alipay.sofa.registry.jraft.repository.impl.InterfaceAppsRaftRepository;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : xingpeng
 * @date : 2021-07-28 20:09
 **/
public class AppRevisionRepositoryTest extends AbstractRaftTestBase {

    private AppRevisionRaftRepository appRevisionRaftRepository;

    private InterfaceAppsRaftRepository interfaceAppsRaftRepository;

    private AppRevisionHeartbeatRaftRepository appRevisionHeartbeatRaftRepository;

    private DefaultCommonConfig defaultCommonConfig;

    private List<AppRevision> appRevisionList;

    private static final Integer APP_REVISION_SIZE = 1;

    private RheaKVStore rheaKVStore;

    @Before
    public void buildAppRevision() {
        appRevisionHeartbeatRaftRepository = applicationContext.getBean(AppRevisionHeartbeatRaftRepository.class);
        interfaceAppsRaftRepository = applicationContext.getBean(InterfaceAppsRaftRepository.class);
        appRevisionRaftRepository = applicationContext.getBean(AppRevisionRaftRepository.class);
        defaultCommonConfig = applicationContext.getBean(DefaultCommonConfig.class);
        rheaKVStore = applicationContext.getBean(RheaKVStore.class);

        appRevisionList = new ArrayList<>();
        for (int i = 1; i <= APP_REVISION_SIZE; i++) {
            long l = System.currentTimeMillis();
            String suffix = l + "-" + i;

            String appname = "foo" + suffix;
            String revision = "1111" + suffix;

            AppRevision appRevision = new AppRevision();
            appRevision.setAppName(appname);
            appRevision.setRevision(revision);
            appRevision.setClientVersion("1.0");

            Map<String, List<String>> baseParams = Maps.newHashMap();
            baseParams.put(
                    "metaBaseParam1",
                    new ArrayList<String>() {
                        {
                            add("metaBaseValue1");
                        }
                    });
            appRevision.setBaseParams(baseParams);

            Map<String, AppRevisionInterface> interfaceMap = Maps.newHashMap();
            String dataInfo1 =
                    DataInfo.toDataInfoId(
                            "func1" + suffix, ValueConstants.DEFAULT_GROUP, ValueConstants.DEFAULT_INSTANCE_ID);
            String dataInfo2 =
                    DataInfo.toDataInfoId(
                            "func2" + suffix, ValueConstants.DEFAULT_GROUP, ValueConstants.DEFAULT_INSTANCE_ID);

            AppRevisionInterface inf1 = new AppRevisionInterface();
            AppRevisionInterface inf2 = new AppRevisionInterface();
            interfaceMap.put(dataInfo1, inf1);
            interfaceMap.put(dataInfo2, inf2);
            appRevision.setInterfaceMap(interfaceMap);

            inf1.setId("1");
            Map<String, List<String>> serviceParams1 = new HashMap<String, List<String>>();
            serviceParams1.put(
                    "metaParam2",
                    new ArrayList<String>() {
                        {
                            add("metaValue2");
                        }
                    });
            inf1.setServiceParams(serviceParams1);

            inf2.setId("2");
            Map<String, List<String>> serviceParams2 = new HashMap<String, List<String>>();
            serviceParams1.put(
                    "metaParam3",
                    new ArrayList<String>() {
                        {
                            add("metaValue3");
                        }
                    });
            inf1.setServiceParams(serviceParams2);

            appRevisionList.add(appRevision);
        }
    }

    @Test
    public void registerAndQuery() throws Exception {
        // register
        for (AppRevision appRevisionRegister : appRevisionList) {
            appRevisionRaftRepository.register(appRevisionRegister);
        }

        // query app_revision
        for (AppRevision appRevisionRegister : appRevisionList) {
            AppRevision revision =
                    appRevisionRaftRepository.queryRevision(appRevisionRegister.getRevision());
            Assert.assertEquals(appRevisionRegister.getAppName(), revision.getAppName());
        }

        // query by interface
        for (AppRevision appRevisionRegister : appRevisionList) {
            for (Map.Entry<String, AppRevisionInterface> entry :
                    appRevisionRegister.getInterfaceMap().entrySet()) {
                String dataInfoId = entry.getKey();
                InterfaceMapping appNames = interfaceAppsRaftRepository.getAppNames(dataInfoId);
                Assert.assertTrue(appNames.getNanosVersion() < 0);
                Assert.assertTrue(appNames.getApps().size() == 1);
                Assert.assertTrue(appNames.getApps().contains(appRevisionRegister.getAppName()));
            }
        }
    }




    
}