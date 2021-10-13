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
package com.alipay.sofa.registry.compress;

import com.alipay.sofa.registry.cache.Sizer;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.CounterFunc;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

public final class CompressUtils {
  private CompressUtils() {}

  private static final Logger LOG = LoggerFactory.getLogger("COMPRESS");

  private static final Map<String, Compressor> compressorMap =
      new HashMap<String, Compressor>() {
        {
          final Compressor gzipCompressor = new Compressor.GzipCompressor();
          final Compressor zstdCompressor = new Compressor.ZstdCompressor();

          put(gzipCompressor.getEncoding(), gzipCompressor);
          put(zstdCompressor.getEncoding(), zstdCompressor);
        }
      };

  public static Compressor mustGet(String encode) {
    Compressor compressor = compressorMap.get(encode);
    Assert.notNull(compressor, StringFormatter.format("compress {} not found", encode));
    return compressor;
  }

  public static Compressor find(String[] acceptEncodes) {
    return find(acceptEncodes, Collections.emptySet());
  }

  public static Compressor find(String[] acceptEncodes, Set<String> forbidEncodes) {
    if (ArrayUtils.isEmpty(acceptEncodes)) {
      return null;
    }
    for (String encoding : acceptEncodes) {
      if (forbidEncodes.contains(encoding)) {
        continue;
      }
      Compressor compressor = compressorMap.get(encoding);
      if (compressor != null) {
        return compressor;
      }
    }
    LOG.warn(
        "accept encoding {} not in available compressors", StringUtils.join(acceptEncodes, ","));
    return null;
  }

  public static <V extends Sizer> CompressCachedExecutor<V> newCachedExecutor(
      String name, long silentMs, long maxWeight) {
    CompressCachedExecutor<V> cachedExecutor =
        new CompressCachedExecutor<>(name, silentMs, maxWeight);
    CounterFunc cacheCounter =
        CounterFunc.build()
            .namespace("compress")
            .subsystem("cache")
            .name(name)
            .labelNames("type")
            .help(StringFormatter.format("compress cache {} hit or missing", name))
            .create()
            .register();
    cacheCounter.labels("hit").func(cachedExecutor::getHitCount);
    cacheCounter.labels("missing").func(cachedExecutor::getMissingCount);
    return cachedExecutor;
  }

  public static String normalizeEncode(String encode) {
    if (StringUtils.isBlank(encode)) {
      return "plain";
    }
    return encode;
  }
}
