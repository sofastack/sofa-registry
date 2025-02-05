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
package com.alipay.sofa.registry.server.test;

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_DATA_CENTER;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_ZONE;

import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.integration.RegistryApplication;
import com.alipay.sofa.registry.util.FileUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author xuanbei
 * @since 2019/3/9
 */
public class TestRegistryMain {
  public static final String LOCAL_ADDRESS = NetUtil.getLocalAddress().getHostAddress();

  private Map<String, String> configs = new HashMap<>();

  public TestRegistryMain() {
    configs.put("nodes.metaNode", DEFAULT_DATA_CENTER + ":" + LOCAL_ADDRESS);
    configs.put("nodes.localDataCenter", DEFAULT_DATA_CENTER);
    configs.put("nodes.localRegion", DEFAULT_ZONE);
    configs.put("nodes.localSegmentRegions=DEFAULT_ZONE", DEFAULT_ZONE);
  }

  public void startRegistry() throws Exception {
    for (Map.Entry<String, String> entry : configs.entrySet()) {
      System.setProperty(entry.getKey(), entry.getValue());
    }
    FileUtils.forceDelete(new File(System.getProperty("user.home") + File.separator + "raftData"));
    RegistryApplication.main(new String[] {});
    Thread.sleep(3000);
  }

  public void stopRegistry() throws Exception {
    RegistryApplication.stop();
  }

  public void startRegistryWithConfig(Map<String, String> configs) throws Exception {
    this.configs.putAll(configs);
    this.startRegistry();
  }

  public ConfigurableApplicationContext getMetaApplicationContext() {
    return RegistryApplication.getMetaApplicationContext();
  }

  public ConfigurableApplicationContext getSessionApplicationContext() {
    return RegistryApplication.getSessionApplicationContext();
  }

  public ConfigurableApplicationContext getDataApplicationContext() {
    return RegistryApplication.getDataApplicationContext();
  }
}
