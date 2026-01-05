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
package com.alipay.sofa.registry.server.session.push;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public final class ChangeKey implements Comparable<ChangeKey> {
  final String dataInfoId;
  final Set<String> dataCenters;
  final String sortedDCMark;

  ChangeKey(Set<String> dataCenters, String dataInfoId) {
    this.dataCenters = dataCenters;
    this.dataInfoId = dataInfoId;
    if (CollectionUtils.isEmpty(dataCenters)) {
      this.sortedDCMark = "";
    } else {
      if (dataCenters.size() > 1) {
        this.sortedDCMark =
            dataCenters.stream().sorted(Comparator.reverseOrder()).collect(Collectors.joining(":"));
      } else {
        this.sortedDCMark = dataCenters.iterator().next();
      }
    }
  }

  @Override
  public String toString() {
    return dataInfoId + "@" + dataCenters;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    ChangeKey otherChangeKey = (ChangeKey) other;
    if (!StringUtils.equals(this.dataInfoId, otherChangeKey.dataInfoId)) {
      return false;
    }

    return StringUtils.equals(this.sortedDCMark, otherChangeKey.sortedDCMark);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataInfoId, dataCenters);
  }

  @Override
  public int compareTo(ChangeKey other) {
    int compare = this.dataInfoId.compareTo(other.dataInfoId);
    if (compare != 0) {
      return compare;
    }
    return this.sortedDCMark.compareTo(other.sortedDCMark);
  }
}
