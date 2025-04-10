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
package com.alipay.sofa.registry.server.session.converter.pb;

import com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb;
import com.alipay.sofa.registry.compress.*;
import com.alipay.sofa.registry.util.SystemUtils;

/**
 * @author huicha
 * @date 2025/3/21
 */
public class DeltaReceivedDataConvertor {

  private static final String KEY_COMPRESS_PUSH_CACHE_CAPACITY = "registry.compress.push.capacity";

  public static final CompressCachedExecutor<CompressedItem> compressCachedExecutor =
      CompressUtils.newCachedExecutor(
          "delta_compress_push",
          60 * 1000,
          SystemUtils.getSystemInteger(KEY_COMPRESS_PUSH_CACHE_CAPACITY, 1024 * 1024 * 384));

  public static CompressedItem compressed(
      String segment,
      String dataId,
      String instanceId,
      String group,
      long version,
      FullReceivedDataBodyPb fullReceivedDataBodyPb,
      ZoneCount[] zoneCounts,
      Compressor compressor)
      throws Exception {
    return compressCachedExecutor.execute(
        CompressPushKey.of(
            segment, dataId, instanceId, group, version, zoneCounts, compressor.getEncoding()),
        () -> {
          byte[] bodyData = fullReceivedDataBodyPb.toByteArray();
          byte[] compressed = compressor.compress(bodyData);
          return new CompressedItem(compressed, bodyData.length, compressor.getEncoding());
        });
  }
}
