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
package com.alipay.sofa.registry.server.shared.util;

import com.alipay.remoting.serialization.HessianSerializer;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.compress.CompressCachedExecutor;
import com.alipay.sofa.registry.compress.CompressUtils;
import com.alipay.sofa.registry.compress.CompressedItem;
import com.alipay.sofa.registry.compress.Compressor;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.SystemUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author xuanbei
 * @since 2019/2/12
 */
public final class DatumUtils {
  private static final String KEY_COMPRESS_DATUM_CACHE_CAPACITY =
      "registry.compress.datum.capacity";
  private static final String KEY_DECOMPRESS_DATUM_CACHE_CAPACITY =
      "registry.decompress.datum.capacity";

  public static final CompressCachedExecutor<CompressedItem> compressCachedExecutor =
      CompressUtils.newCachedExecutor(
          "datum_compress",
          60 * 1000,
          SystemUtils.getSystemInteger(KEY_COMPRESS_DATUM_CACHE_CAPACITY, 1024 * 1024 * 128));

  public static final CompressCachedExecutor<SubPublisherList> decompressCachedExecutor =
      CompressUtils.newCachedExecutor(
          "datum_decompress",
          60 * 1000,
          SystemUtils.getSystemInteger(KEY_DECOMPRESS_DATUM_CACHE_CAPACITY, 1024 * 1024 * 384));

  public static final HessianSerializer serializer = new HessianSerializer();

  private DatumUtils() {}

  public static Map<String, DatumVersion> intern(Map<String, DatumVersion> versionMap) {
    Map<String, DatumVersion> ret = Maps.newHashMapWithExpectedSize(versionMap.size());
    versionMap.forEach((k, v) -> ret.put(WordCache.getWordCache(k), v));
    return ret;
  }

  public static Map<String, Long> getVersions(Map<String, Datum> datumMap) {
    Map<String, Long> versions = Maps.newHashMapWithExpectedSize(datumMap.size());
    datumMap.forEach((k, v) -> versions.put(k, v.getVersion()));
    return versions;
  }

  public static MultiSubDatum newEmptyMultiSubDatum(
      Subscriber subscriber, Set<String> datacenters, long version) {

    Map<String, SubDatum> subDatumMap = Maps.newHashMapWithExpectedSize(datacenters.size());
    for (String datacenter : datacenters) {
      subDatumMap.put(datacenter, newEmptySubDatum(subscriber, datacenter, version));
    }

    return new MultiSubDatum(subscriber.getDataInfoId(), subDatumMap);
  }

  public static SubDatum newEmptySubDatum(Subscriber subscriber, String datacenter, long version) {
    return SubDatum.emptyOf(
        subscriber.getDataInfoId(),
        datacenter,
        version,
        subscriber.getDataId(),
        subscriber.getInstanceId(),
        subscriber.getGroup());
  }

  public static SubDatum of(Datum datum) {
    List<SubPublisher> publishers = Lists.newArrayListWithCapacity(datum.publisherSize());
    for (Publisher publisher : datum.getPubMap().values()) {
      final URL srcAddress = publisher.getSourceAddress();
      // temp publisher the srcAddress maybe null
      final String srcAddressString = srcAddress == null ? null : srcAddress.buildAddressString();
      publishers.add(
          new SubPublisher(
              publisher.getRegisterId(),
              publisher.getCell(),
              publisher.getDataList(),
              publisher.getClientId(),
              publisher.getVersion(),
              srcAddressString,
              publisher.getRegisterTimestamp(),
              publisher.getPublishSource()));
    }
    return SubDatum.normalOf(
        datum.getDataInfoId(),
        datum.getDataCenter(),
        datum.getVersion(),
        publishers,
        datum.getDataId(),
        datum.getInstanceId(),
        datum.getGroup(),
        datum.getRecentVersions());
  }

  public static long DataBoxListSize(List<DataBox> boxes) {
    if (CollectionUtils.isEmpty(boxes)) {
      return 0;
    }
    long sum = 0;
    for (DataBox box : boxes) {
      if (box == null || box.getData() == null) {
        continue;
      }
      sum += box.getData().length();
    }
    return sum;
  }

  public static long ServerDataBoxListSize(List<ServerDataBox> boxes) {
    if (CollectionUtils.isEmpty(boxes)) {
      return 0;
    }
    long sum = 0;
    for (ServerDataBox box : boxes) {
      if (box == null) {
        continue;
      }
      box.object2bytes();
      sum += box.byteSize();
    }
    return sum;
  }

  public static SubDatum compressSubDatum(SubDatum datum, Compressor compressor) {
    if (compressor == null || datum == null) {
      return datum;
    }
    CompressedItem compressedItem;
    try {
      compressedItem =
          compressCachedExecutor.execute(
              datum.compressKey(compressor.getEncoding()),
              () -> {
                List<SubPublisher> pubs = datum.mustGetPublishers();
                byte[] data = serializer.serialize(new SubPublisherList(pubs));
                byte[] compressed = compressor.compress(data);
                return new CompressedItem(compressed, data.length, compressor.getEncoding());
              });
    } catch (Throwable e) {
      throw new RuntimeException("compress publishers failed", e);
    }
    return SubDatum.zipOf(
        datum.getDataInfoId(),
        datum.getDataCenter(),
        datum.getVersion(),
        datum.getDataId(),
        datum.getInstanceId(),
        datum.getGroup(),
        datum.getRecentVersions(),
        new ZipSubPublisherList(
            compressedItem.getCompressedData(),
            compressedItem.getOriginSize(),
            compressedItem.getEncoding(),
            datum.getPubNum()));
  }

  public static MultiSubDatum decompressMultiSubDatum(MultiSubDatum multiSubDatum) {
    ParaCheckUtil.checkNotEmpty(multiSubDatum.getDatumMap(), "multiSubDatum.datumMap");
    Map<String, SubDatum> datumMap =
        Maps.newHashMapWithExpectedSize(multiSubDatum.getDatumMap().size());
    for (Entry<String, SubDatum> entry : multiSubDatum.getDatumMap().entrySet()) {
      datumMap.put(entry.getKey(), decompressSubDatum(entry.getValue()));
    }
    return new MultiSubDatum(multiSubDatum.getDataInfoId(), datumMap);
  }

  public static SubDatum decompressSubDatum(SubDatum datum) {
    ZipSubPublisherList zip = datum.getZipPublishers();
    if (zip == null) {
      return datum;
    }
    Compressor compressor = CompressUtils.mustGet(zip.getEncoding());
    SubPublisherList subPublisherList;
    try {
      subPublisherList =
          decompressCachedExecutor.execute(
              datum.compressKey(compressor.getEncoding()),
              () -> {
                byte[] data = compressor.decompress(zip.getCompressedData(), zip.getOriginSize());
                return serializer.deserialize(data, SubPublisherList.className);
              });
    } catch (Throwable e) {
      throw new RuntimeException("decompress publishers failed", e);
    }
    return SubDatum.normalOf(
        datum.getDataInfoId(),
        datum.getDataCenter(),
        datum.getVersion(),
        subPublisherList.getPubs(),
        datum.getDataId(),
        datum.getInstanceId(),
        datum.getGroup(),
        datum.getRecentVersions());
  }
}
