package com.alipay.sofa.registry.server.session.push;

import java.util.Objects;
import java.util.Set;

public final class ChangeKey {
    final String dataInfoId;
    final Set<String> dataCenters;

    ChangeKey(Set<String> dataCenters, String dataInfoId) {
      this.dataCenters = dataCenters;
      this.dataInfoId = dataInfoId;
    }

    @Override
    public String toString() {
      return dataInfoId + "@" + dataCenters;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ChangeKey changeKey = (ChangeKey) o;
      return Objects.equals(dataInfoId, changeKey.dataInfoId)
          && Objects.equals(dataCenters, changeKey.dataCenters);
    }

    @Override
    public int hashCode() {
      return Objects.hash(dataInfoId, dataCenters);
    }
  }