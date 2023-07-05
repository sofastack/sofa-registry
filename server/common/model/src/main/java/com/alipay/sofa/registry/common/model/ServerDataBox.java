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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author zhuoyu.sjw
 * @version $Id: ServerDataBox.java, v 0.1 2018-03-03 17:44 zhuoyu.sjw Exp $$
 */
public class ServerDataBox implements Serializable {

  /** UID */
  private static final long serialVersionUID = 2817539491173993030L;
  /** */
  private static final int SERIALIZED_BY_JAVA = 1;
  /** Null for locally instantiated, otherwise for internalized */
  private byte[] bytes;
  /** Only available if bytes != null */
  private int serialization;
  /** Actual object, lazy deserialized */
  private Object object;

  /** Instantiates a new DataBox. */
  public ServerDataBox() {}

  /**
   * Instantiates a new DataBox.
   *
   * @param object the object
   */
  public ServerDataBox(Object object) {
    this.object = object;
  }

  /**
   * Instantiates a new DataBox.
   *
   * @param bytes the bytes
   */
  public ServerDataBox(byte[] bytes) {
    this.bytes = bytes;
    this.serialization = SERIALIZED_BY_JAVA;
  }

  /**
   * Is in bytes boolean.
   *
   * @return boolean boolean
   */
  @JsonIgnore
  public boolean isInBytes() {
    return bytes != null;
  }

  /**
   * Only when isInBytes() == false
   *
   * @return Object object
   */
  public Object getObject() {
    return object;
  }

  /**
   * transfer bytes to object
   *
   * @return Object object
   * @throws IOException the io exception
   * @throws ClassNotFoundException the class not found exception
   */
  public Object extract() throws IOException, ClassNotFoundException {
    if (object == null && isInBytes()) {
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      if (serialization != SERIALIZED_BY_JAVA) {
        throw new IOException("Unsupported serialization type: " + serialization);
      }
      ServerDataBoxInputStream input = null;
      try {
        input = new ServerDataBoxInputStream(bis);
        object = input.readObject();
      } finally {
        if (input != null) {
          input.close();
        }
      }
    }

    return object;
  }

  /**
   * change object to bytes
   *
   * @return NSwizzle swizzle
   */
  public ServerDataBox object2bytes() {
    if (!isInBytes()) {
      bytes = getBytes(object);
      serialization = SERIALIZED_BY_JAVA;
    }
    return this;
  }

  public static byte[] getBytes(Object object) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream javaos = null;
    try {
      javaos = new ObjectOutputStream(bos);
      javaos.writeObject(object);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } finally {
      try {
        if (null != javaos) {
          javaos.close();
        }
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }
    return bos.toByteArray();
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    serialization = in.readByte(); // Read serialization type
    int size = in.readInt(); // Read byte stream size
    bytes = new byte[size];
    in.readFully(bytes); // Read the byte stream
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    if (isInBytes()) {
      out.writeByte(serialization); // Write serialization type
      out.writeInt(bytes.length); // Write byte stream size
      out.write(bytes); // Write the byte stream
    } else {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream javaos = new ObjectOutputStream(bos);
      try {
        javaos.writeObject(object);
      } finally {
        try {
          javaos.close();
        } catch (IOException ioe) {
          throw new RuntimeException(ioe);
        }
      }
      out.writeByte(SERIALIZED_BY_JAVA); // Write serialization type
      out.writeInt(bos.size()); // Write byte stream size
      out.write(bos.toByteArray()); // Write the byte stream
    }
  }

  public int byteSize() {
    final byte[] b = bytes;
    return b != null ? b.length : 0;
  }

  /**
   * Get bytes byte [ ].
   *
   * @return byte[] byte [ ]
   */
  public byte[] getBytes() {
    return bytes;
  }

  /**
   * Set bytes byte [ ].
   *
   * @param bytes bytes
   */
  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Gets serialization.
   *
   * @return int serialization
   */
  public int getSerialization() {
    return this.serialization;
  }

  /**
   * Sets serialization.
   *
   * @param serial the serial
   */
  public void setSerialization(int serial) {
    this.serialization = serial;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServerDataBox that = (ServerDataBox) o;
    return Arrays.equals(bytes, that.bytes) && Objects.equal(object, that.object);
  }
}
