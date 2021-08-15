package com.alipay.sofa.registry.jraft.repository;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.jraft.AbstractRaftTestBase;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jraft.config.DefaultConfigs;
import com.alipay.sofa.registry.jraft.domain.InterfaceAppsDomain;
import com.alipay.sofa.registry.jraft.repository.impl.InterfaceAppsRaftRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : xingpeng
 * @date : 2021-07-30 08:36
 **/
public class InterfaceAppsRaftRepositoryTest extends AbstractRaftTestBase {
    private RheaKVStore rheaKVStore;

    private InterfaceAppsRaftRepository interfaceAppsRaftRepository;

    private static final String INTERFACE_APPS="InterfaceApps";

    /** map: <interface, interfaceAppsDomain> */
    protected Map<String, InterfaceAppsDomain> interfaceAppsMap = new ConcurrentHashMap<>();

    private DefaultCommonConfig defaultCommonConfig;

    @Before
    public void before(){
        interfaceAppsRaftRepository = applicationContext.getBean(InterfaceAppsRaftRepository.class);
        rheaKVStore = applicationContext.getBean(RheaKVStore.class);
        defaultCommonConfig = applicationContext.getBean(DefaultCommonConfig.class);
    }

    @Test
    public void testGetAppNames(){
        byte[] interfaceAppsBytes = rheaKVStore.bGet(INTERFACE_APPS);
        String app = "saveApp";
        List<String> services = new ArrayList<>();
        try{
            interfaceAppsMap = CommandCodec.decodeCommand(interfaceAppsBytes, interfaceAppsMap.getClass());
        }catch (NullPointerException e) {
            //LOG.info("INTERFACE_APPS RheaKV is empty");
        }
        for (int i = 0; i < 100; i++) {
            services.add(i + "batchSaveService-" + System.currentTimeMillis());
        }
        for (String interfaceName : new HashSet<>(services)){
            InterfaceAppsDomain interfaceAppsDomain = new InterfaceAppsDomain(defaultCommonConfig.getClusterId(),
                    interfaceName,
                    app,
                    new Timestamp(System.currentTimeMillis())
            );
            interfaceAppsMap.put(interfaceName,interfaceAppsDomain);
        }
        rheaKVStore.bPut(INTERFACE_APPS,CommandCodec.encodeCommand(interfaceAppsMap));
        
        for (String service : services) {
            InterfaceMapping appNames = interfaceAppsRaftRepository.getAppNames(service);
            Assert.assertEquals(0, appNames.getApps().size());
        }
    }

}
