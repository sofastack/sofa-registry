package com.alipay.sofa.registry.server.session.push;

public interface ChangeHandler {

  boolean onChange(String dataInfoId, TriggerPushContext changeCtx);

}