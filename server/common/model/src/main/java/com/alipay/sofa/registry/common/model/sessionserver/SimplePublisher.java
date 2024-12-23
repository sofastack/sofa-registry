package com.alipay.sofa.registry.common.model.sessionserver;

import com.alipay.sofa.registry.util.StringFormatter;

import java.io.Serializable;

/**
 * @author huicha
 * @date 2024/12/23
 */
public final class SimplePublisher implements Serializable {

  private static final long serialVersionUID = 6861155219172594665L;

  private final String clientId;

  private final String sourceAddress;

  private final String appName;

  public SimplePublisher(String clientId, String sourceAddress, String appName) {
    this.clientId = clientId;
    this.sourceAddress = sourceAddress;
    this.appName = appName;
  }

  public String getClientId() {
    return clientId;
  }

  public String getSourceAddress() {
    return sourceAddress;
  }

  public String getAppName() {
    return appName;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
            "SimplePublisher{app={},clientId={},add={}}", appName, clientId, sourceAddress);
  }
}
