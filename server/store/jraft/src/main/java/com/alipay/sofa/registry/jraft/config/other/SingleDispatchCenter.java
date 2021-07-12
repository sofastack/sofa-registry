package com.alipay.sofa.registry.jraft.config.other;

import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RocksDBOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.StoreEngineOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.util.Endpoint;
import com.alipay.sofa.registry.jraft.exception.RheaKVDriverException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * @author : xingpeng
 * @date : 2021-06-26 13:06
 **/
//public class SingleDispatchCenter extends AbstractDispatchCenter{
//    private static final Logger LOG =
//            LoggerFactory.getLogger("JRaft-DISPATCH", "[SingleDispatchCenter]");
//
//    private boolean flag=true;
//
//    private SingleRheaKVDriverConfigBean rheaKVDriverConfigBean;
//
//    @Override
//    public void init(){
//        List<RheaKVDriver> beanListOfType = null;
//        try{
//            beanListOfType = getBeanListOfType(RheaKVDriver.class);
//            System.out.println(beanListOfType.size());
//        }catch (NullPointerException e){
//            flag=false;
//            LOG.info("[RheaKVDriverInit] start DefaultRheaKVDriver");
//        }
//        if(flag && !beanListOfType.isEmpty()){
//            CheckRheaKVDriverConfigBean(beanListOfType);
//            rheaKVDriverConfigBean= (SingleRheaKVDriverConfigBean) beanListOfType.get(0);
//        }else{
//            rheaKVDriverConfigBean=DefaultRheaKVStoreOptionsInit();
//        }
//    }
//
//    @Override
//    public void CheckRheaKVDriverConfigBean(List<RheaKVDriver> beanList){
//        int size=beanList.size();
//        if(size==1){
//            SingleRheaKVDriverConfigBean rheaKVDriver = (SingleRheaKVDriverConfigBean)beanList.get(0);
//            PlacementDriverOptions placementDriverOptions = rheaKVDriver.getRheaKVStoreOptions().getPlacementDriverOptions();
//            if(!placementDriverOptions.isFake()){
//                throw new RheaKVDriverException("Single RheaKVDriver PlacementDriver");
//            }
//        }else{
//            throw new RheaKVDriverException("Single RheaKVDriver has to be unique");
//        }
//    }
//
//
//    protected SingleRheaKVDriverConfigBean DefaultRheaKVStoreOptionsInit(){
//        rheaKVDriverConfigBean=new SingleRheaKVDriverConfigBean();
//        String address = DefaultConfigs.ADDRESS;
//        StoreEngineOptions storeOpts = StoreEngineOptionsConfigured.newConfigured()
//                .withStorageType(StorageType.RocksDB)
//                .withRocksDBOptions(RocksDBOptionsConfigured.newConfigured().withDbPath(DefaultConfigs.DB_PATH).config())
//                .withRaftDataPath(DefaultConfigs.RAFT_DATA_PATH)
//                .withServerAddress(new Endpoint(address, 8181))
//                .config();
//
//        PlacementDriverOptions pdOpts = PlacementDriverOptionsConfigured.newConfigured()
//                .withFake(true)
//                .config();
//
//        RheaKVStoreOptions rheaKVStoreOptions = RheaKVStoreOptionsConfigured.newConfigured()
//                .withStoreEngineOptions(storeOpts)
//                .withPlacementDriverOptions(pdOpts)
//                .withInitialServerList(address+":"+"8181")
//                .config();
//
//        rheaKVDriverConfigBean.setRheaKVStoreOptions(rheaKVStoreOptions);
//
//        return rheaKVDriverConfigBean;
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext=applicationContext;
//    }
//
//    @Override
//    public RheaKVDriver getRheaKVDriver() {
//        return rheaKVDriverConfigBean;
//    }
//}
