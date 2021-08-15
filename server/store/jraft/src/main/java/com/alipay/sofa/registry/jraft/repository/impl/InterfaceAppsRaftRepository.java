/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.jraft.repository.impl;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jraft.domain.InterfaceAppsDomain;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.TimestampUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : xingpeng
 * @date : 2021-07-05 11:45
 **/
public class InterfaceAppsRaftRepository implements InterfaceAppsRepository {
  protected static final Logger LOG = LoggerFactory.getLogger(InterfaceAppsRaftRepository.class);

  @Autowired
  private RheaKVStore rheaKVStore;

  private static final String INTERFACE_APPS="InterfaceApps";

  /** map: <interface, interfaceAppsDomain> */
  protected Map<String, InterfaceAppsDomain> interfaceAppsMap = new ConcurrentHashMap<>();

  @Autowired
  private DefaultCommonConfig defaultCommonConfig;

  @Override
  public void loadMetadata() {
    // FIXME
  }

  @Override
  public InterfaceMapping getAppNames(String dataInfoId) {
    byte[] interfaceAppsBytes = rheaKVStore.bGet(INTERFACE_APPS);
    InterfaceMapping appNames = null;
    InterfaceAppsDomain interfaceAppsDomain = null;
    InterfaceAppsDomain newInterface = null;
    
    try{
      interfaceAppsMap = CommandCodec.decodeCommand(interfaceAppsBytes, interfaceAppsMap.getClass());
      interfaceAppsDomain = interfaceAppsMap.get(dataInfoId);
    }catch (NullPointerException e) {
      LOG.info("INTERFACE_APPS RheaKV is empty");
    }
    try{
      appNames = new InterfaceMapping(interfaceAppsDomain.getNanosVersion(),interfaceAppsDomain.getApps());
    }catch (NullPointerException e){
      //LOG.info("NanosVersion :{} ,Apps is null",interfaceAppsDomain.getNanosVersion());
    }

    //存在返回app集合
    if(appNames!=null){
      return appNames;
    }

    //插入新interface并返回interfacemapping
    appNames=new InterfaceMapping(-1);
    if(interfaceAppsDomain!=null){
      interfaceAppsDomain.setGmtModify(new Timestamp(System.currentTimeMillis()));
      newInterface = new InterfaceAppsDomain(defaultCommonConfig.getClusterId(),
              dataInfoId,
              interfaceAppsDomain.getAppName(),
              interfaceAppsDomain.isReference(),
              interfaceAppsDomain.getHashcode(),
              interfaceAppsDomain.getGmtModify(),
              appNames.getNanosVersion(),
              appNames.getApps()
      );
      LOG.info("update interfaceMapping {}, {}", dataInfoId, appNames);
    }else{
      newInterface = new InterfaceAppsDomain(defaultCommonConfig.getClusterId(),
              dataInfoId,
              appNames.getNanosVersion(),
              appNames.getApps()
      );
    }

    interfaceAppsMap.put(dataInfoId, newInterface);
    rheaKVStore.bPut(INTERFACE_APPS,CommandCodec.encodeCommand(interfaceAppsMap));
    return appNames;
  }
  
  /** refresh interfaceNames index */
  public synchronized void triggerRefreshCache(InterfaceAppsDomain domain) {
    InterfaceAppsDomain interfaceMapping = interfaceAppsMap.get(domain.getInterfaceName());
    //System.out.println(interfaceMapping);
    if (interfaceMapping != null) {
      InterfaceMapping map = new InterfaceMapping(interfaceMapping.getNanosVersion(),interfaceMapping.getApps());
      final long nanosLong = TimestampUtil.getNanosLong(domain.getGmtModify());
      if (map == null) {
        if (domain.isReference()) {
          map = new InterfaceMapping(nanosLong, domain.getAppName());
        } else {
          map = new InterfaceMapping(nanosLong);
        }
        if (LOG.isInfoEnabled()) {
          LOG.info(
                  "refresh interface: {}, ref: {}, app: {}, mapping: {}",
                  domain.getInterfaceName(),
                  domain.isReference(),
                  domain.getAppName(),
                  map);
        }
        domain.setGmtModify(new Timestamp(System.currentTimeMillis()));
        InterfaceAppsDomain interfaceAppsDomain=new InterfaceAppsDomain(
                domain.getDataCenter(),
                domain.getInterfaceName(),
                domain.getAppName(),
                domain.isReference(),
                domain.getHashcode(),
                domain.getGmtModify(),
                map.getNanosVersion(),
                map.getApps()
        );
        interfaceAppsMap.put(domain.getInterfaceName(),interfaceAppsDomain);
        return;
      }
      //判断版本信息
      if(nanosLong>map.getNanosVersion()){
        InterfaceMapping newMapping=null;
        //判断关联
        if(domain.isReference()){
          newMapping=new InterfaceMapping(nanosLong,map.getApps(),domain.getAppName());
        }else{
          Set<String> prev = Sets.newHashSet(map.getApps());
          prev.remove(domain.getAppName());
          newMapping=new InterfaceMapping(nanosLong,prev,domain.getAppName());
        }
        if(LOG.isInfoEnabled()){
          LOG.info(
                  "update interface mapping: {}, ref: {}, app: {}, newMapping: {}, oldMapping: {}",
                  domain.getInterfaceName(),
                  domain.isReference(),
                  domain.getAppName(),
                  newMapping,
                  map);
        }
        domain.setGmtModify(new Timestamp(System.currentTimeMillis()));
        InterfaceAppsDomain newInterfaceAppsDomain=new InterfaceAppsDomain(
                domain.getDataCenter(),
                domain.getInterfaceName(),
                domain.getAppName(),
                domain.isReference(),
                domain.getHashcode(),
                domain.getGmtModify(),
                newMapping.getNanosVersion(),
                newMapping.getApps()
        );
        interfaceAppsMap.put(domain.getInterfaceName(),newInterfaceAppsDomain);
      }else{
        LOG.error(
                "[IgnoreUpdateCache]ignored refresh index, interfac={}, newVersion={} , current mapping={}",
                domain.getInterfaceName(),
                nanosLong,
                map);
      }
    }else{
      interfaceAppsMap.put(domain.getInterfaceName(),domain);
    }
  }

}
