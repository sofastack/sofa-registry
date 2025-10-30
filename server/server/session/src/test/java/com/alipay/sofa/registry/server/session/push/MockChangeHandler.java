package com.alipay.sofa.registry.server.session.push;

public class MockChangeHandler implements ChangeHandler {
  @Override
  public boolean onChange(String dataInfoId, TriggerPushContext changeCtx) {
    return true;
  }
}