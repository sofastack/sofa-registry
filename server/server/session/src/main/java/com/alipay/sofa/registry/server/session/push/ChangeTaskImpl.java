package com.alipay.sofa.registry.server.session.push;

import com.alipay.sofa.registry.util.StringFormatter;

public final class ChangeTaskImpl implements ChangeTask<ChangeKey> {
    final TriggerPushContext changeCtx;
    final ChangeKey key;
    final ChangeHandler changeHandler;
    final long expireTimestamp;
    long expireDeadlineTimestamp;

    ChangeTaskImpl(
        ChangeKey key,
        TriggerPushContext changeCtx,
        ChangeHandler changeHandler,
        long expireTimestamp) {
      this.key = key;
      this.changeHandler = changeHandler;
      this.changeCtx = changeCtx;
      this.expireTimestamp = expireTimestamp;
    }

    void doChange() {
      changeHandler.onChange(key.dataInfoId, changeCtx);
    }

    @Override
    public String toString() {
      return StringFormatter.format(
          "ChangeTask{{},ver={},expire={},deadline={}}",
          key,
          changeCtx.getExpectDatumVersion(),
          expireTimestamp,
          expireDeadlineTimestamp);
    }

    @Override
    public ChangeKey key() {
      return this.key;
    }

    @Override
    public long deadline() {
      return this.expireTimestamp;
    }
  }