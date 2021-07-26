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

import com.alipay.sofa.registry.concurrent.CachedExecutor;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.CounterFunc;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

public class CompressUtils {
  private static final Logger LOG = LoggerFactory.getLogger("COMPRESS");
  public static final CachedExecutor<String, CompressedItem> cachedExecutor =
      // 30s expire duration, 500MB total cache size
      new CachedExecutor<>(
          1000 * 30, 1024 * 1024 * 500, (String k, CompressedItem v) -> k.length() + v.size());
  private static final CounterFunc cacheCounter =
      CounterFunc.build()
          .namespace("compress")
          .name("cache")
          .labelNames("type")
          .help("compress cache hit or missing")
          .create()
          .register();

  static {
    cacheCounter.labels("hit").func(cachedExecutor::getHitCount);
    cacheCounter.labels("missing").func(cachedExecutor::getMissingCount);
  }

  private static final Map<String, Compressor> compressorMap =
      new HashMap<String, Compressor>() {
        {
          final Compressor gzipCompressor = new Compressor.GzipCompressor();
          final Compressor zstdCompressor = new Compressor.ZstdCompressor();

          put(gzipCompressor.getEncoding(), gzipCompressor);
          put(zstdCompressor.getEncoding(), zstdCompressor);
        }
      };

  public static Compressor get(String acceptEncoding) {
    return get(acceptEncoding, Collections.emptySet());
  }

  public static Compressor get(String acceptEncoding, Set<String> forbidEncodes) {
    if (StringUtils.isBlank(acceptEncoding)) {
      return null;
    }
    String[] encodings = StringUtils.split(acceptEncoding, ",");
    if (encodings.length == 0) {
      return null;
    }
    for (String encoding : encodings) {
      if (forbidEncodes.contains(encoding)) {
        continue;
      }
      Compressor compressor = compressorMap.get(encoding);
      if (compressor != null) {
        return compressor;
      }
    }
    LOG.warn("accept encoding {} not in available compressors");
    return null;
  }

  public static class CompressedItem {
    private final byte[] compressedData;
    private final int originSize;

    public CompressedItem(byte[] compressedData, int originSize) {
      this.compressedData = compressedData;
      this.originSize = originSize;
    }

    public int size() {
      return compressedData.length + 16;
    }

    public int getOriginSize() {
      return originSize;
    }

    public byte[] getCompressedData() {
      return compressedData;
    }
  }
}
