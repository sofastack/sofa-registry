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
package com.alipay.sofa.registry.core.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaojian.xj
 * @version : MultiSegmentData.java, v 0.1 2022年07月17日 16:18 xiaojian.xj Exp $
 */
public class MultiSegmentData implements Serializable {
  private static final long serialVersionUID = -2814374534088238588L;

  private String segment;

  private byte[] zipData;

  private Map<String /*zone*/, List<DataBox>> unzipData = new HashMap<String, List<DataBox>>();

  private String encoding;

  private long version;

  private Map<String, Integer> dataCount = new HashMap<String, Integer>();

  public MultiSegmentData() {}

  public MultiSegmentData(String segment, long version) {
    this.segment = segment;
    this.version = version;
  }

  /**
   * Getter method for property <tt>segment</tt>.
   *
   * @return property value of segment
   */
  public String getSegment() {
    return segment;
  }

  /**
   * Setter method for property <tt>segment</tt>.
   *
   * @param segment value to be assigned to property segment
   */
  public void setSegment(String segment) {
    this.segment = segment;
  }

  /**
   * Getter method for property <tt>zipData</tt>.
   *
   * @return property value of zipData
   */
  public byte[] getZipData() {
    return zipData;
  }

  /**
   * Setter method for property <tt>zipData</tt>.
   *
   * @param zipData value to be assigned to property zipData
   */
  public void setZipData(byte[] zipData) {
    this.zipData = zipData;
  }

  /**
   * Getter method for property <tt>unzipData</tt>.
   *
   * @return property value of unzipData
   */
  public Map<String, List<DataBox>> getUnzipData() {
    return unzipData;
  }

  /**
   * Setter method for property <tt>unzipData</tt>.
   *
   * @param unzipData value to be assigned to property unzipData
   */
  public void setUnzipData(Map<String, List<DataBox>> unzipData) {
    this.unzipData = unzipData;
  }

  /**
   * Getter method for property <tt>encoding</tt>.
   *
   * @return property value of encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Setter method for property <tt>encoding</tt>.
   *
   * @param encoding value to be assigned to property encoding
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public long getVersion() {
    return version;
  }

  /**
   * Setter method for property <tt>version</tt>.
   *
   * @param version value to be assigned to property version
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * Getter method for property <tt>dataCount</tt>.
   *
   * @return property value of dataCount
   */
  public Map<String, Integer> getDataCount() {
    return dataCount;
  }

  /**
   * Setter method for property <tt>dataCount</tt>.
   *
   * @param dataCount value to be assigned to property dataCount
   */
  public void setDataCount(Map<String, Integer> dataCount) {
    this.dataCount = dataCount;
  }

  @Override
  public String toString() {
    return "MultiSegmentData{"
        + "segment='"
        + segment
        + '\''
        + ", zipData="
        + Arrays.toString(zipData)
        + ", unzipData="
        + unzipData
        + ", encoding='"
        + encoding
        + '\''
        + ", version="
        + version
        + ", dataCount="
        + dataCount
        + '}';
  }
}
