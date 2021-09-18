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
import java.util.Collections;
import java.util.Set;
import org.springframework.util.CollectionUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompressPushSwitch {
  private boolean enabled = false;

  @JsonSetter(nulls = Nulls.SKIP)
  private int compressMinSize = CompressConstants.defaultCompressPushMinSize;

  @JsonSetter(nulls = Nulls.SKIP)
  private Set<String> forbidEncodes = Collections.emptySet();

  @JsonSetter(nulls = Nulls.SKIP)
  private Set<String> enabledSessions = Collections.emptySet();

  @JsonSetter(nulls = Nulls.SKIP)
  private Set<String> enabledClients = Collections.emptySet();

  public CompressPushSwitch() {}

  public static CompressPushSwitch defaultSwitch() {
    return new CompressPushSwitch();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public long getCompressMinSize() {
    return compressMinSize;
  }

  public void setCompressMinSize(int size) {
    compressMinSize = size;
  }

  public Set<String> getForbidEncodes() {
    return forbidEncodes;
  }

  public Set<String> getEnabledSessions() {
    return enabledSessions;
  }

  public void setForbidEncodes(Set<String> encodes) {
    if (CollectionUtils.isEmpty(encodes)) {
      forbidEncodes = Collections.emptySet();
    } else {
      forbidEncodes = encodes;
    }
  }

  public void setEnabledSessions(Set<String> sessions) {
    if (CollectionUtils.isEmpty(sessions)) {
      enabledSessions = Collections.emptySet();
    } else {
      enabledSessions = sessions;
    }
  }

  public Set<String> getEnabledClients() {
    return enabledClients;
  }

  public void setEnabledClients(Set<String> clients) {
    if (CollectionUtils.isEmpty(clients)) {
      enabledClients = Collections.emptySet();
    } else {
      enabledClients = clients;
    }
  }

  @Override
  public String toString() {
    return "CompressPushSwitch{"
        + "enabled="
        + enabled
        + ", compressMinSize="
        + compressMinSize
        + ", forbidEncodes="
        + forbidEncodes
        + ", enabledSessions="
        + enabledSessions
        + ", enabledClients="
        + enabledClients
        + '}';
  }
}
