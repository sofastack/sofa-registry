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
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jraft.config.MetadataConfig;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.repository.AppRevisionHeartbeatRepository;
import com.alipay.sofa.registry.util.BatchCallableRunnable;
import com.alipay.sofa.registry.util.MathUtils;
import com.alipay.sofa.registry.util.SingleFlight;
import com.google.common.collect.Sets;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : xingpeng
 * @date : 2021-07-05 11:45
 **/
public class AppRevisionHeartbeatRaftRepository implements AppRevisionHeartbeatRepository {
  private static final Logger LOG = LoggerFactory.getLogger(AppRevisionHeartbeatRaftRepository.class);

  @Autowired
  private AppRevisionRaftRepository appRevisionRaftRepository;

  @Autowired
  private DefaultCommonConfig defaultCommonConfig;

  @Autowired
  private RheaKVStore rheaKVStore;

//  @Autowired
//  private AppRevisionHeartbeatBatchCallable appRevisionHeartbeatBatchCallable;

  private SingleFlight singleFlight = new SingleFlight();

  private static final Integer heartbeatCheckerSize = 1000;

  private static final String APP_REVISION="AppRevision";

  /**dataCenter,AppRevision*/
  private Map<String, AppRevision> appRevisionMap=new ConcurrentHashMap<>();

  @Override
  public void doAppRevisionHeartbeat() {
    try {
      singleFlight.execute(
              "app_revision_heartbeat",
              () -> {
                //查询数据库
                byte[] appRevisionBytes = rheaKVStore.bGet(APP_REVISION);
                Map<String, AppRevision> appRevisionInfoMap = CommandCodec.decodeCommand(appRevisionBytes, appRevisionMap.getClass());

                //获取appRevision列表
                Set<String> heartbeatSet =
                        appRevisionRaftRepository
                                .getHeartbeatSet()
                                .getAndSet(new ConcurrentHashMap<>().newKeySet());

                //更新lastTime
                for (String revision : heartbeatSet) {
                    AppRevision appRevision = appRevisionInfoMap.get(defaultCommonConfig.getClusterId());
                    if(appRevision.getRevision().equals(revision)){
                        appRevision.setLastHeartbeat(new Date());
                        appRevisionInfoMap.put(defaultCommonConfig.getClusterId(),appRevision);
                    }
                }
                return null;
              });
    } catch (Exception e) {
      LOG.error("app_revision heartbeat error.", e);
    }

  }

  @Override
  public void doHeartbeatCacheChecker() {
    try{
      //获取数据库数据
      byte[] appRevisionBytes = rheaKVStore.bGet(APP_REVISION);
      Map<String, AppRevision> appRevisionInfoMap = CommandCodec.decodeCommand(appRevisionBytes, appRevisionMap.getClass());

      Set<String> heartbeatSet = appRevisionRaftRepository.getHeartbeatSet().get();
      List<String> revisions = new ArrayList(heartbeatSet);
      List<String> exists = new ArrayList<>();
      int round = MathUtils.divideCeil(revisions.size(), heartbeatCheckerSize);
      for (int i = 0; i < round; i++) {
        int start = i * heartbeatCheckerSize;
        int end =
                start + heartbeatCheckerSize < revisions.size()
                        ? start + heartbeatCheckerSize
                        : revisions.size();
        String revision=null ;
        List<String> subRevisions = revisions.subList(start, end);
        if(appRevisionInfoMap.get(defaultCommonConfig.getClusterId())!=null){
            revision = appRevisionInfoMap.get(defaultCommonConfig.getClusterId()).getRevision();
        }
        if(subRevisions!=null && subRevisions.size()>0){
          if(subRevisions.contains(revision)){
            exists.add(revision);
          }
        }

        Sets.SetView<String> difference = Sets.difference(new HashSet<>(revisions), new HashSet<>(exists));
        LOG.info("[doHeartbeatCacheChecker] reduces heartbeat size: {}", difference.size());
        appRevisionRaftRepository.invalidateHeartbeat(difference);
        
      }
    }catch (Exception e){
      LOG.error("app_revision heartbeat cache checker error.", e);
    }
  }

  @Override
  public void doAppRevisionGc(int silenceHour) {
    try {
      singleFlight.execute(
              "app_revision_gc",
              () -> {
                Date date = DateUtils.addHours(new Date(), -silenceHour);
                AppRevision appRevision=null;
                //获取数据库数据
                byte[] appRevisionBytes = rheaKVStore.bGet(APP_REVISION);
                try{
                  appRevisionMap = CommandCodec.decodeCommand(appRevisionBytes, appRevisionMap.getClass());
                }catch (NullPointerException e){
                    LOG.info("APP_REVISION RheaKV is empty");
                }
                try{
                    appRevision = appRevisionMap.get(defaultCommonConfig.getClusterId());
                    //System.out.println(appRevision);
                }catch (NullPointerException e){
                    LOG.info("dataCenter : {} , without AppRevision",defaultCommonConfig.getClusterId());
                }

                if(appRevision.getLastHeartbeat().before(date)){
                  if (LOG.isInfoEnabled()) {
                    LOG.info("app_revision tobe gc dataCenter: {}, revision: {}", defaultCommonConfig.getClusterId(),appRevision.getRevision());
                  }
                  appRevisionMap.remove(defaultCommonConfig.getClusterId());

                }
                return null;
              });
        rheaKVStore.bPut(APP_REVISION,CommandCodec.encodeCommand(appRevisionMap));
    } catch (Exception e) {
      LOG.error("app_revision gc error.", e);
    }
  }
}
