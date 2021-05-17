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
package com.alipay.sofa.registry.jdbc.repository.batch;

import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.Register.REVISION_HEARTBEAT_COUNTER;

import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.mapper.AppRevisionMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.BatchCallableRunnable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionHeartbeatBatchCallable.java, v 0.1 2021年02月09日 17:52 xiaojian.xj Exp $
 */
public class AppRevisionHeartbeatBatchCallable extends BatchCallableRunnable<String, String> {

  private static final Logger LOG =
      LoggerFactory.getLogger("METADATA-EXCHANGE", "[AppRevisionHeartbeatBatch]");

  @Autowired private AppRevisionMapper appRevisionMapper;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  public AppRevisionHeartbeatBatchCallable() {
    super(100, TimeUnit.MILLISECONDS, 200);
  }
  /**
   * batch update gmt_modified
   *
   * @param tasks
   * @return
   */
  @Override
  public boolean batchProcess(List<TaskEvent> tasks) {

    if (CollectionUtils.isEmpty(tasks)) {
      return true;
    }
    if (LOG.isInfoEnabled()) {
      LOG.info("update app_revision gmt_modified, task size: " + tasks.size());
    }
    List<String> revisions =
        tasks.stream().map(task -> task.getData()).collect(Collectors.toList());

    appRevisionMapper.batchHeartbeat(defaultCommonConfig.getClusterId(), revisions);
    REVISION_HEARTBEAT_COUNTER.inc();
    tasks.forEach(
        taskEvent -> {
          InvokeFuture<String> future = taskEvent.getFuture();
          future.putResponse(taskEvent.getData());
        });

    return true;
  }
}
