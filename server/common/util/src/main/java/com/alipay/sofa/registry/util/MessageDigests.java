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
package com.alipay.sofa.registry.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This MessageDigests class provides convenience methods for obtaining thread local {@link
 * MessageDigest} instances for MD5, SHA-1, and SHA-256 message digests.
 *
 * @author zhuoyu.sjw
 * @version $Id: MessageDigests.java, v 0.1 2016-11-01 16:29 zhuoyu.sjw Exp $$
 */
public final class MessageDigests {

  /** MD5 */
  private static final ThreadLocal<MessageDigest> MD5_DIGEST =
      createThreadLocalMessageDigest("MD5");
  /** SHA_1 */
  private static final ThreadLocal<MessageDigest> SHA_1_DIGEST =
      createThreadLocalMessageDigest("SHA-1");
  /** SHA_256 */
  private static final ThreadLocal<MessageDigest> SHA_256_DIGEST =
      createThreadLocalMessageDigest("SHA-256");
  /** */
  private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

  /**
   * Create thread local message digest thread local.
   *
   * @param digest the digest
   * @return thread local
   */
  private static ThreadLocal<MessageDigest> createThreadLocalMessageDigest(final String digest) {
    return ThreadLocal.withInitial(
        () -> {
          try {
            return MessageDigest.getInstance(digest);
          } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                "unexpected exception creating MessageDigest instance for [" + digest + "]", e);
          }
        });
  }

  /** @return a thread local MD5 {@link MessageDigest} instance */
  public static MessageDigest md5() {
    return get(MD5_DIGEST);
  }

  /** @return a thread local SHA_1 {@link MessageDigest} instance */
  public static MessageDigest sha1() {
    return get(SHA_1_DIGEST);
  }

  /** @return a thread local SHA_256 {@link MessageDigest} instance */
  public static MessageDigest sha256() {
    return get(SHA_256_DIGEST);
  }

  /**
   * get and reset thread local {@link MessageDigest} instance
   *
   * @param messageDigest threadLocalMessageDigest
   * @return a thread local {@link MessageDigest} instance
   */
  private static MessageDigest get(ThreadLocal<MessageDigest> messageDigest) {
    MessageDigest instance = messageDigest.get();
    instance.reset();
    return instance;
  }

  public static String getMd5String(String digest) {

    return toHexString(md5().digest(digest.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * Format a byte array as a hex string.
   *
   * @param bytes the input to be represented as hex.
   * @return a hex representation of the input as a String.
   */
  public static String toHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder(2 * bytes.length);

    for (byte b : bytes) {
      sb.append(HEX_DIGITS[b >> 4 & 0xf]).append(HEX_DIGITS[b & 0xf]);
    }

    return sb.toString();
  }
}
