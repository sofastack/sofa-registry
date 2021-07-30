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
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jraft.domain.InterfaceAppsDomain;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.util.TimestampUtil;
import com.sun.xml.internal.bind.v2.runtime.output.StAXExStreamWriterOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author : xingpeng
 * @date : 2021-07-05 11:45
 **/
public class AppRevisionRaftRepository implements AppRevisionRepository {
  private static final Logger LOG = LoggerFactory.getLogger(AppRevisionRaftRepository.class);

  @Autowired
  private RheaKVStore rheaKVStore;

  private static final String APP_REVISION="AppRevision";

  private static final String INTERFACE_APPS="InterfaceApps";

  /**dataCenter,AppRevision*/
  private Map<String, AppRevision> appRevisionMap=new ConcurrentHashMap<>();

  /** map: <revision, AppRevision> */
  private final AtomicReference<ConcurrentHashMap.KeySetView> heartbeatSet =
          new AtomicReference<>();
  
  /** map: <interface, interfaceAppsDomain> */
  protected final Map<String, InterfaceAppsDomain> interfaceAppsMap = new ConcurrentHashMap<>();
  
  @Autowired private DefaultCommonConfig defaultCommonConfig;

  @Resource
  private InterfaceAppsRaftRepository interfaceAppsRaftRepository;

  @Override
  public void register(AppRevision appRevision) {
    //Assert.isNull(appRevision,"RheaKV register app revision error, appRevision is null.");
    System.out.println(appRevision);
    AppRevision appRevisionInfo=null;
    byte[] appRevisionMapBytes = rheaKVStore.bGet(APP_REVISION);
    System.out.println(appRevisionMapBytes==null);

    //查询集群appRevision
    try {
      appRevisionMap = CommandCodec.decodeCommand(appRevisionMapBytes, appRevisionMap.getClass());
      appRevisionInfo = appRevisionMap.get(defaultCommonConfig.getClusterId());
    }catch (NullPointerException e){
      LOG.info("RheaKV is empty");
    }


    if(appRevisionInfo!=null){
      for(Map.Entry<String, AppRevision> app : appRevisionMap.entrySet())  {
        System.out.println(app.getValue());
        System.out.println("================");
      }
//      System.out.println(appRevisionInfo.getDataCenter());
      return;
    }
    
    //插入新AppRevision
    //设置新的dataCenter
    appRevision.setDataCenter(defaultCommonConfig.getClusterId());
    appRevisionMap.put(defaultCommonConfig.getClusterId(),appRevision);


    
    rheaKVStore.bPut(APP_REVISION,CommandCodec.encodeCommand(appRevisionMap));
  }

  //有问题
  @Override
  public void refresh() {
    byte[] interfaceAppsBytes = rheaKVStore.bGet(INTERFACE_APPS);
    Map<String, InterfaceAppsDomain> interfaceAppsInfoMap = CommandCodec.decodeCommand(interfaceAppsBytes, interfaceAppsMap.getClass());
    //获取全部interfaceApps
    List<InterfaceAppsDomain> interfaceAppsList = (List<InterfaceAppsDomain>)interfaceAppsInfoMap.values();

    List<InterfaceAppsDomain> collects = interfaceAppsList
            .stream()
            .filter(e -> e.getDataCenter() == defaultCommonConfig.getClusterId())
            .collect(Collectors.toList());

    //更新interface和app
    //更新rheakv
    for(InterfaceAppsDomain collect : collects){
      interfaceAppsRaftRepository.triggerRefreshCache(collect);
      rheaKVStore.bPut(INTERFACE_APPS,CommandCodec.encodeCommand(interfaceAppsRaftRepository.interfaceAppsMap));
    }
  }

  @Override
  public AppRevision queryRevision(String revision) {
    List<AppRevision> collect = null;
    AppRevision appRevision=null;
    byte[] appRevisionMapBytes = rheaKVStore.bGet(APP_REVISION);
    try{
      appRevisionMap = CommandCodec.decodeCommand(appRevisionMapBytes, appRevisionMap.getClass());
      appRevision = appRevisionMap.get(defaultCommonConfig.getClusterId());
    }catch(NullPointerException e){

    }
    for(Map.Entry<String, AppRevision> app : appRevisionMap.entrySet())  {
      System.out.println(app.getValue());
    }
    System.out.println(appRevision);
    return appRevision;

//    System.out.println(appRevisionMap.get(defaultCommonConfig.getClusterId()).getRevision());
//    System.out.println(revision);
//
//    for(Map.Entry<String, AppRevision> values : appRevisionMap.entrySet()){
//      AppRevision value = values.getValue();
//      if(value.getRevision()==revision) {
//        collect.add(value);
//      }
//    };
//
//    if(collect==null){
//      LOG.info("RheaKV query revision failed, revision: {} not exist in db", revision);
//      return null;
//    }
//    return collect.get(0);
  }

  @Override
  public boolean heartbeat(String revision) {
    if (heartbeatSet.get().contains(revision)) {
      return true;
    }
    byte[] bytes = rheaKVStore.bGet(APP_REVISION);
    Map<String, AppRevision> appRevisionInfoMap = CommandCodec.decodeCommand(bytes, appRevisionMap.getClass());
    AppRevision appRevision = appRevisionInfoMap.get(defaultCommonConfig.getClusterId());

    if(appRevision!=null && appRevision.getRevision()==revision){
      heartbeatSet.get().add(revision);
      return true;
    }
    return false;
  }

  /**
   * Getter method for property <tt>heartbeatMap</tt>.
   *
   * @return property value of heartbeatMap
   */
  public AtomicReference<ConcurrentHashMap.KeySetView> getHeartbeatSet() {
    return heartbeatSet;
  }

  public void invalidateHeartbeat(Collection<String> keys) {
    if (LOG.isInfoEnabled()) {
      LOG.info("Invalidating heartbeat cache keys: {}", keys);
    }
    heartbeatSet.get().removeAll(keys);
  }
}
