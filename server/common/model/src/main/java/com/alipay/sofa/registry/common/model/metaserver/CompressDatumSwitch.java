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
package com.alipay.sofa.registry.common.model.metaserver;

import com.alipay.sofa.registry.compress.CompressConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompressDatumSwitch {
  private boolean enabled = false;

  @JsonSetter(nulls = Nulls.SKIP)
  private int compressMinSize = CompressConstants.defaultCompressDatumMinSize;

  public static CompressDatumSwitch defaultSwitch() {
    return new CompressDatumSwitch();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getCompressMinSize() {
    return compressMinSize;
  }

  public void setCompressMinSize(int compressMinSize) {
    this.compressMinSize = compressMinSize;
  }

  @Override
  public String toString() {
    return "CompressDatumSwitch{"
        + "enabled="
        + enabled
        + ", compressMinSize="
        + compressMinSize
        + '}';
  }
}
