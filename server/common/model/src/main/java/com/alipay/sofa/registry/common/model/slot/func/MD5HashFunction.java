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
package com.alipay.sofa.registry.common.model.slot.func;

import com.alipay.sofa.registry.util.MessageDigests;
import java.nio.charset.Charset;

/**
 * MD5 hash function
 *
 * @author zhuoyu.sjw
 * @version $Id: MD5HashFunction.java, v 0.1 2016-11-01 15:19 zhuoyu.sjw Exp $$
 */
public class MD5HashFunction implements HashFunction {

  /** Default charset of UTF-8 */
  private static final Charset UTF8 = Charset.forName("UTF-8");

  /** Instantiates a new MD5 hash function. */
  public MD5HashFunction() {}

  /** @see HashFunction#hash(Object) */
  @Override
  public int hash(Object s) {
    byte[] hash = MessageDigests.md5().digest(s.toString().getBytes(UTF8));

    // HACK just take the first 4 digits and make it an integer.
    // apparently this is what other algorithms use to turn it into an int
    // value.

    int h0 = (hash[0] & 0xFF);
    int h1 = (hash[1] & 0xFF) << 8;
    int h2 = (hash[2] & 0xFF) << 16;
    int h3 = (hash[3] & 0xFF) << 24;

    return h0 + h1 + h2 + h3;
  }
}
