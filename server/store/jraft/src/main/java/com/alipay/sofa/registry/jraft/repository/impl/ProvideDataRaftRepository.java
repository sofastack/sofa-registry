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
import java.util.concurrent.atomic.AtomicInteger;

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

    @Override
    public boolean put(PersistenceData persistenceData, long expectVersion) {
        byte[] provideDataBytes = rheaKVStore.bGet(PROVIDE_DATA);
        ProvideDataDomain provideDataDomain=null;
        Integer count=0;
        try{
            provideDataMap = CommandCodec.decodeCommand(provideDataBytes, provideDataMap.getClass());
            provideDataDomain = provideDataMap.get(defaultCommonConfig.getClusterId());
        }catch(NullPointerException e){
            LOG.info("PROVIDE_DATA RheaKV is empty");
        }

        String dataKey= PersistenceDataBuilder.getDataInfoId(persistenceData);
        ProvideDataDomain newProvideDataDomain=new ProvideDataDomain(defaultCommonConfig.getClusterId(),
                dataKey,
                persistenceData.getData(),
                new Date(),
                persistenceData.getVersion()
        );

        if(provideDataDomain==null){
            provideDataMap.put(defaultCommonConfig.getClusterId(),newProvideDataDomain);
            count++;
            if (LOG.isInfoEnabled()) {
                LOG.info("save provideData: {}", persistenceData);
            }
        }else{
            //更新
            //System.out.println("进入更新");
            for(Map.Entry<String, ProvideDataDomain> map:provideDataMap.entrySet()){
                if(map.getValue().getDataKey()==dataKey && map.getValue().getDataVersion()==expectVersion){
                    map.getValue().setGmtModified(new Date());
                    map.getValue().setDataVersion(newProvideDataDomain.getDataVersion());
                    map.getValue().setDataValue(newProvideDataDomain.getDataValue());
                    count++;
                    //System.out.println(count.get());
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                                "update provideData: {}, expectVersion: {}",
                                newProvideDataDomain,
                                expectVersion
                        );
                    }
                }
            }
        }
        if(count==0){
            provideDataMap.put(defaultCommonConfig.getClusterId(),newProvideDataDomain);
            count++;
        }

        //存储到内存数据库
        rheaKVStore.bPut(PROVIDE_DATA,CommandCodec.encodeCommand(provideDataMap));
        
        return count>0;
    }

    @Override
    public PersistenceData get(String key) {
        byte[] provideDataBytes = rheaKVStore.bGet(PROVIDE_DATA);
        ProvideDataDomain provideData=null;
        try{
            provideDataMap = CommandCodec.decodeCommand(provideDataBytes, provideDataMap.getClass());
            provideData = provideDataMap.get(defaultCommonConfig.getClusterId());
        }catch(NullPointerException e){
            LOG.info("PROVIDE_DATA RheaKV is empty");
        }
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
        ProvideDataDomain provideDataDomain=null;
        try{
            provideDataMap = CommandCodec.decodeCommand(provideDataBytes, provideDataMap.getClass());
            provideDataDomain = provideDataMap.get(defaultCommonConfig.getClusterId());
        }catch(NullPointerException e){
            LOG.info("PROVIDE_DATA RheaKV is empty");
        }
        if(provideDataDomain==null){
            return false;
        }else{
            if(provideDataDomain.getDataKey().equals(key) && provideDataDomain.getDataVersion()==version){
                provideDataMap.remove(defaultCommonConfig.getClusterId());
                //存储到内存数据库
                rheaKVStore.bPut(PROVIDE_DATA,CommandCodec.encodeCommand(provideDataMap));
                if (LOG.isInfoEnabled()) {
                    LOG.info(
                            "remove provideData, dataCenter: {}, key: {}, version: {}",
                            defaultCommonConfig.getClusterId(),
                            key,
                            version);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<PersistenceData> getAll() {
        byte[] provideDataBytes = rheaKVStore.bGet(PROVIDE_DATA);
        Collection<PersistenceData> responses = new ArrayList<>();
        ProvideDataDomain provideDataDomain=null;
        try{
            provideDataMap = CommandCodec.decodeCommand(provideDataBytes, provideDataMap.getClass());
            provideDataDomain = provideDataMap.get(defaultCommonConfig.getClusterId());
        }catch (NullPointerException e){
            LOG.info("PROVIDE_DATA RheaKV is empty");
        }

        if(provideDataDomain==null){
            return null;
        }else{
            DataInfo dataInfo = DataInfo.valueOf(provideDataDomain.getDataKey());
            PersistenceData persistenceData = new PersistenceData();
            persistenceData.setDataId(dataInfo.getDataId());
            persistenceData.setGroup(dataInfo.getGroup());
            persistenceData.setInstanceId(dataInfo.getInstanceId());
            persistenceData.setData(provideDataDomain.getDataValue());
            persistenceData.setVersion(provideDataDomain.getDataVersion());

            responses.add(persistenceData);
        }
        return responses;
    }
}
