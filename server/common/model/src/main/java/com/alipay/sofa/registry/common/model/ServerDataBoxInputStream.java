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
package com.alipay.sofa.registry.common.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * The type DataBox input stream.
 *
 * @author zhuoyu.sjw
 * @version $Id : ServerDataBoxInputStream.java, v 0.1 2017-11-28 17:48 zhuoyu.sjw Exp $$
 */
public class ServerDataBoxInputStream extends ObjectInputStream {

  /**
   * Instantiates a new DataBox input stream.
   *
   * @param in the in
   * @throws IOException the io exception
   */
  public ServerDataBoxInputStream(InputStream in) throws IOException {
    super(in);
  }

  /** @see ObjectInputStream#resolveClass(ObjectStreamClass) */
  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc)
      throws IOException, ClassNotFoundException {
    String name = desc.getName();
    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      return Class.forName(name, false, classLoader);
    } catch (ClassNotFoundException ex) {
      return super.resolveClass(desc);
    }
  }
}
