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

import com.alipay.sofa.registry.TestUtils;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CompressorTest {
  private static final Logger LOG = LoggerFactory.getLogger(CompressorTest.class);
  private static byte[] exampleData;

  @BeforeClass
  public static void beforeClass() throws IOException {
    InputStream ins = ClassLoader.getSystemClassLoader().getResourceAsStream("push_example.json");
    exampleData = CharStreams.toString(new InputStreamReader(ins)).getBytes(StandardCharsets.UTF_8);
  }

  @Test
  public void testCompressor() throws Exception {
    final byte[] src = exampleData;
    byte[] zdata;
    Compressor c;

    c = CompressUtils.find(new String[] {CompressConstants.encodingGzip});
    Assert.assertNotNull(c);
    zdata = c.compress(src);
    Assert.assertArrayEquals(src, c.decompress(zdata, src.length));
    LOG.info("gzip src: {} dst: {}", src.length, zdata.length);

    c = CompressUtils.find(new String[] {CompressConstants.encodingZstd});
    Assert.assertNotNull(c);
    zdata = c.compress(src);
    Assert.assertArrayEquals(src, c.decompress(zdata, src.length));
    LOG.info("zstd src: {} dst: {}", src.length, zdata.length);
  }

  @Test
  public void testBenchmarkGzip() throws Exception {
    final byte[] src = exampleData;
    new Random().nextBytes(src);
    Compressor c;

    c = CompressUtils.find(new String[] {CompressConstants.encodingGzip});
    Assert.assertNotNull(c);
    byte[] dst = c.compress(src);
    for (int i = 0; i < 20; i++) {
      // warmup
      c.compress(src);
    }

    long start = System.currentTimeMillis();
    int times = 100;
    for (int i = 0; i < times; i++) {
      c.compress(src);
    }
    double duration = System.currentTimeMillis() - start;
    LOG.info("gzip compress total: {}, per: {}", duration, duration / times);

    start = System.currentTimeMillis();
    for (int i = 0; i < times; i++) {
      c.decompress(dst, src.length);
    }
    duration = System.currentTimeMillis() - start;
    LOG.info("gzip decompress total: {}, per: {}", duration, duration / times);
  }

  @Test
  public void testBenchmarkZstd() throws Exception {
    final byte[] src = exampleData;
    new Random().nextBytes(src);
    Compressor c;

    c = CompressUtils.find(new String[] {CompressConstants.encodingZstd});
    Assert.assertNotNull(c);
    byte[] dst = c.compress(src);
    for (int i = 0; i < 100; i++) {
      // warmup
      c.compress(src);
    }

    long start = System.currentTimeMillis();
    int times = 1000;
    for (int i = 0; i < times; i++) {
      c.compress(src);
    }
    double duration = System.currentTimeMillis() - start;
    LOG.info("zstd compress total: {}, per: {}", duration, duration / times);

    start = System.currentTimeMillis();
    for (int i = 0; i < times; i++) {
      c.decompress(dst, src.length);
    }
    duration = System.currentTimeMillis() - start;
    LOG.info("zstd decompress total: {}, per: {}", duration, duration / times);
  }

  @Test
  public void testGet() {
    TestUtils.assertException(
        IllegalArgumentException.class,
        () -> {
          CompressUtils.mustGet("asd");
        });
    CompressUtils.mustGet("zstd");

    Assert.assertNull(CompressUtils.find(new String[] {"asd"}));
    Assert.assertNull(CompressUtils.find(null));
    Assert.assertNull(CompressUtils.find(new String[0]));
    Assert.assertNotNull(CompressUtils.find(new String[] {"zstd"}));
    Assert.assertNull(CompressUtils.find(new String[] {"zstd"}, Sets.newHashSet("zstd")));
    Assert.assertEquals("plain", CompressUtils.normalizeEncode(""));
    Assert.assertEquals("zstd", CompressUtils.normalizeEncode("zstd"));
  }

  @Test
  public void testNewExec() {
    Random r = new Random();
    CompressUtils.newCachedExecutor(Long.toString(System.currentTimeMillis()), 30, 100);
  }
}
