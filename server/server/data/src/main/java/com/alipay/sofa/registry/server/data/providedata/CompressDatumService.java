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
package com.alipay.sofa.registry.server.data.providedata;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.CompressDatumSwitch;
import com.alipay.sofa.registry.common.model.metaserver.CompressPushSwitch;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.compress.CompressUtils;
import com.alipay.sofa.registry.compress.Compressor;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.util.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class CompressDatumService
    extends AbstractFetchSystemPropertyService<CompressDatumService.CompressStorage> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CompressPushSwitch.class);

  @Autowired private DataServerConfig dataServerConfig;

  public CompressDatumService() {
    super(
        ValueConstants.COMPRESS_DATUM_SWITCH_DATA_ID,
        new CompressStorage(INIT_VERSION, CompressDatumSwitch.defaultSwitch()));
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return dataServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected boolean doProcess(CompressStorage expect, ProvideData data) {
    final String switchString = ProvideData.toString(data);
    if (StringUtils.isBlank(switchString)) {
      LOGGER.info("Fetch compress datum switch content empty");
      return true;
    }
    try {
      CompressDatumSwitch compressDatumSwitch =
          JsonUtils.read(switchString, CompressDatumSwitch.class);
      CompressStorage update = new CompressStorage(data.getVersion(), compressDatumSwitch);
      if (!compareAndSet(expect, update)) {
        return false;
      }
      LOGGER.info(
          "Fetch compress datum switch, prev={}, current={}",
          expect.compressDatumSwitch,
          switchString);
      return true;
    } catch (Throwable e) {
      LOGGER.error("Fetch compress datum switch error", e);
    }
    return false;
  }

  protected CompressDatumSwitch getCompressSwitch() {
    return storage.get().compressDatumSwitch;
  }

  public Compressor getCompressor(SubDatum datum, String[] encodes) {
    if (datum == null) {
      return null;
    }
    CompressDatumSwitch compressDatumSwitch = getCompressSwitch();
    if (!compressDatumSwitch.isEnabled()) {
      return null;
    }
    datum.mustUnzipped();
    if (datum.getDataBoxBytes() < compressDatumSwitch.getCompressMinSize()) {
      return null;
    }
    return CompressUtils.find(encodes);
  }

  protected static class CompressStorage extends SystemDataStorage {
    protected final CompressDatumSwitch compressDatumSwitch;

    public CompressStorage(long version, CompressDatumSwitch compressDatumSwitch) {
      super(version);
      this.compressDatumSwitch = compressDatumSwitch;
    }
  }
}
