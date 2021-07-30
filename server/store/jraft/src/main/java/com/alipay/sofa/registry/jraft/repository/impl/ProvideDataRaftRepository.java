package com.alipay.sofa.registry.jraft.repository.impl;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jraft.domain.ProvideDataDomain;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : xingpeng
 * @date : 2021-07-30 20:26
 **/
public class ProvideDataRaftRepository implements ProvideDataRepository {
    private static final Logger LOG = LoggerFactory.getLogger("META-PROVIDEDATA", "[ProvideData]");

    @Autowired
    private RheaKVStore rheaKVStore;

    @Autowired
    private DefaultCommonConfig defaultCommonConfig;

    private static final String PROVIDE_DATA="ProvideData";

    /** Map<dataCenter,provideData> */
    private Map<String, ProvideDataDomain> provideDataMap=new ConcurrentHashMap<>();

    //未完成
    @Override
    public boolean put(PersistenceData persistenceData, long expectVersion) {
        byte[] provideDataBytes = rheaKVStore.bGet(PROVIDE_DATA);
        ProvideDataDomain provideDataDomain=null;
        try{
            provideDataMap = CommandCodec.decodeCommand(provideDataBytes, provideDataMap.getClass());
            provideDataDomain = provideDataMap.get(defaultCommonConfig.getClusterId());
        }catch(NullPointerException e){

        }
        String dataKey= PersistenceDataBuilder.getDataInfoId(persistenceData);

        ProvideDataDomain newProvideDataDomain=new ProvideDataDomain(defaultCommonConfig.getClusterId(),
                dataKey,
                persistenceData.getData(),
                persistenceData.getVersion()
        );
        int count=0;

        if(provideDataDomain==null){
            provideDataMap.put(defaultCommonConfig.getClusterId(),newProvideDataDomain);
            count++;
            if (LOG.isInfoEnabled()) {
                LOG.info("save provideData: {}, affect rows: {}", persistenceData);
            }
        }else{
            //更新
            for(Map.Entry<String, ProvideDataDomain> map:provideDataMap.entrySet()){
                if(map.getValue().getDataKey()==newProvideDataDomain.getDataKey() && map.getValue().getDataVersion()==expectVersion){
                    map.getValue().setGmtModified(new Date());
                    map.getValue().setDataVersion(newProvideDataDomain.getDataVersion());
                    map.getValue().setDataValue(newProvideDataDomain.getDataValue());
                    count++;
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(
                        "update provideData: {}, expectVersion: {}",
                        newProvideDataDomain,
                        expectVersion
                );
            }
        }

        if(count == 0){

        }

        return count>0;
    }

    @Override
    public PersistenceData get(String key) {
        byte[] provideDataBytes = rheaKVStore.bGet(PROVIDE_DATA);
        try{
            provideDataMap = CommandCodec.decodeCommand(provideDataBytes, provideDataMap.getClass());
        }catch(NullPointerException e){

        }
        ProvideDataDomain provideData = provideDataMap.get(defaultCommonConfig.getClusterId());
        if(provideData==null){
            return null;
        }
        DataInfo dataInfo = DataInfo.valueOf(provideData.getDataKey());
        PersistenceData persistenceData = new PersistenceData();
        persistenceData.setDataId(dataInfo.getDataId());
        persistenceData.setGroup(dataInfo.getGroup());
        persistenceData.setInstanceId(dataInfo.getInstanceId());
        persistenceData.setData(provideData.getDataValue());
        persistenceData.setVersion(provideData.getDataVersion());
        return persistenceData;
    }

    @Override
    public boolean remove(String key, long version) {
        byte[] provideDataBytes = rheaKVStore.bGet(PROVIDE_DATA);
        try{
            provideDataMap = CommandCodec.decodeCommand(provideDataBytes, provideDataMap.getClass());
        }catch(NullPointerException e){

        }
        ProvideDataDomain provideDataDomain = provideDataMap.get(defaultCommonConfig.getClusterId());
        if(provideDataDomain==null){
            return false;
        }else{
            if(provideDataDomain.getDataVersion()==version && provideDataDomain.getDataKey()==key){
                provideDataMap.remove(defaultCommonConfig.getClusterId());
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<PersistenceData> getAll() {
        Collection<PersistenceData> responses = new ArrayList<>();

        return null;
    }
}
