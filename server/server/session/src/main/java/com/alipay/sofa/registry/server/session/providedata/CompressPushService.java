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
package com.alipay.sofa.registry.server.session.providedata;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.CompressPushSwitch;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.compress.CompressUtils;
import com.alipay.sofa.registry.compress.Compressor;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.util.JsonUtils;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class CompressPushService
    extends AbstractFetchSystemPropertyService<CompressPushService.CompressStorage> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CompressPushSwitch.class);

  @Autowired private SessionServerConfig sessionServerConfig;

  public CompressPushService() {
    super(
        ValueConstants.COMPRESS_PUSH_SWITCH_DATA_ID,
        new CompressStorage(INIT_VERSION, CompressPushSwitch.defaultSwitch()));
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected boolean doProcess(CompressStorage expect, ProvideData data) {
    final String switchString = ProvideData.toString(data);
    if (StringUtils.isBlank(switchString)) {
      LOGGER.info("Fetch session push compressed enabled content empty");
      return true;
    }
    try {
      CompressPushSwitch compressPushSwitch =
          JsonUtils.read(switchString, CompressPushSwitch.class);
      CompressStorage update = new CompressStorage(data.getVersion(), compressPushSwitch);
      if (!compareAndSet(expect, update)) {
        return false;
      }
      LOGGER.info(
          "Fetch session push compressed, prev={}, current={}",
          expect.compressPushSwitch,
          switchString);
      return true;
    } catch (Throwable e) {
      LOGGER.error("Fetch session push compressed enabled error", e);
    }
    return false;
  }

  public CompressPushSwitch getCompressSwitch() {
    return storage.get().compressPushSwitch;
  }

  public Compressor getCompressor(
      Map<String, List<DataBox>> data, String[] acceptEncodes, String clientIp) {
    CompressPushSwitch compressPushSwitch = getCompressSwitch();
    if (!compressEnabled(compressPushSwitch, clientIp)) {
      return null;
    }
    if (dataBoxesMapSize(data) < compressPushSwitch.getCompressMinSize()) {
      return null;
    }
    return CompressUtils.find(acceptEncodes, compressPushSwitch.getForbidEncodes());
  }

  private static boolean compressEnabled(CompressPushSwitch compressPushSwitch, String clientIp) {
    if (compressPushSwitch.isEnabled()) {
      return true;
    }
    if (compressPushSwitch
        .getEnabledSessions()
        .contains(NetUtil.getLocalAddress().getHostAddress())) {
      return true;
    }
    if (compressPushSwitch.getEnabledClients().contains(clientIp)) {
      return true;
    }
    return false;
  }

  private static int dataBoxesMapSize(Map<String, List<DataBox>> dataBoxesMap) {
    int size = 0;
    for (Map.Entry<String, List<DataBox>> boxesEntry : dataBoxesMap.entrySet()) {
      size += boxesEntry.getKey().length();
      for (DataBox box : boxesEntry.getValue()) {
        if (!StringUtils.isBlank(box.getData())) {
          size += box.getData().length();
        }
      }
    }
    return size;
  }

  protected static class CompressStorage extends SystemDataStorage {
    protected final CompressPushSwitch compressPushSwitch;

    public CompressStorage(long version, CompressPushSwitch compressPushSwitch) {
      super(version);
      this.compressPushSwitch = compressPushSwitch;
    }
  }
}
