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

import com.github.luben.zstd.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.*;

public abstract class Compressor {
  public abstract String getEncoding();

  public abstract byte[] compress(byte[] data) throws Exception;

  public abstract byte[] decompress(byte[] data, int decompressedSize) throws Exception;

  private static final ThreadLocal<ByteArrayOutputStream> bosRepo =
      ThreadLocal.withInitial(() -> new ByteArrayOutputStream(1024 * 64));

  public static class GzipCompressor extends Compressor {

    @Override
    public String getEncoding() {
      return CompressConstants.encodingGzip;
    }

    @Override
    public byte[] compress(byte[] data) throws Exception {
      ByteArrayOutputStream bos = bosRepo.get();
      bos.reset();
      try (GZIPOutputStream gzipOs = new GZIPOutputStream(bos)) {
        gzipOs.write(data);
        gzipOs.close();
        return bos.toByteArray();
      }
    }

    @Override
    public byte[] decompress(byte[] data, int decompressedSize) throws Exception {
      ByteArrayOutputStream bos = bosRepo.get();
      bos.reset();
      try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(data))) {
        final byte[] buffer = new byte[4096];
        int len;
        while ((len = gis.read(buffer)) > 0) {
          bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
      }
    }
  }

  public static class ZstdCompressor extends Compressor {

    @Override
    public String getEncoding() {
      return CompressConstants.encodingZstd;
    }

    @Override
    public byte[] compress(byte[] data) throws Exception {
      return Zstd.compress(data);
    }

    @Override
    public byte[] decompress(byte[] data, int decompressedSize) throws Exception {
      return Zstd.decompress(data, decompressedSize);
    }
  }
}
