package com.alipay.sofa.registry.jraft.config.other;

import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.registry.jraft.exception.RheaKVDriverException;

import java.util.List;

/**
 * @author : xingpeng
 * @date : 2021-06-24 20:35
 **/
//public class SingleRheaKVDriverConfigBean implements SingleRheaKVDriverConfig {
//
//   private StoreEngineOptions storeEngineOptions;
//
//   private RheaKVStoreOptions rheaKVStoreOptions;
//
//   private SingleRheaKVDriverConfigBean rheaKVDriver;
//
//    public void setStoreEngineOptions(StoreEngineOptions storeEngineOptions) {
//        this.storeEngineOptions = storeEngineOptions;
//    }
//
//    public void setRheaKVStoreOptions(RheaKVStoreOptions rheaKVStoreOptions) {
//        this.rheaKVStoreOptions = rheaKVStoreOptions;
//    }
//
//    @Override
//    public StoreEngineOptions getStoreEngineOptions() {
//        return storeEngineOptions;
//    }
//
//    @Override
//    public RheaKVStoreOptions getRheaKVStoreOptions() {
//        return rheaKVStoreOptions;
//    }
//
//    @Override
//    public void CheckRheaKVDriverConfigBean(List<RheaKVDriver> beanList) {
//        if(beanList.size()==1){
//            if(beanList.get(0) instanceof SingleRheaKVDriverConfigBean) {
//                rheaKVDriver = (SingleRheaKVDriverConfigBean) beanList.get(0);
//                PlacementDriverOptions placementDriverOptions = rheaKVDriver.getRheaKVStoreOptions().getPlacementDriverOptions();
//                if (!placementDriverOptions.isFake()) {
//                    throw new RheaKVDriverException("Single RheaKVDriver PlacementDriver has to be fake");
//                }
//            }else {
//                throw new RheaKVDriverException("RheaKVDriver has to be SingleRheaKVDriverConfigBean");
//            }
//        }else{
//            throw new RheaKVDriverException("Single RheaKVDriver has to be unique");
//        }
//    }
//
//    @Override
//    public RheaKVDriver getRheaKVDriver() {
//        return rheaKVDriver;
//    }
//}
