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
package com.alipay.sofa.registry.client.provider;

import com.alipay.sofa.registry.client.api.Register;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.registration.BaseRegistration;
import com.alipay.sofa.registry.client.auth.AuthManager;
import com.alipay.sofa.registry.client.constants.ValueConstants;
import com.alipay.sofa.registry.client.constants.VersionConstants;
import com.alipay.sofa.registry.client.util.StringUtils;
import com.alipay.sofa.registry.core.model.BaseRegister;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The type Internal register.
 *
 * @author zhuoyu.sjw
 * @version $Id : AbstractInternalRegister.java, v 0.1 2017-11-23 20:52 zhuoyu.sjw Exp $$
 */
public abstract class AbstractInternalRegister implements Register {

  /** */
  private final AtomicLong initialVersion = new AtomicLong(VersionConstants.UNINITIALIZED_VERSION);
  /** */
  private AuthManager authManager;
  /** */
  private volatile boolean registered = false;
  /** */
  private volatile boolean enabled = true;
  /** */
  private volatile boolean refused = false;
  /** */
  private AtomicLong pubVersion = new AtomicLong(VersionConstants.UNINITIALIZED_VERSION);
  /** */
  private AtomicLong ackVersion = new AtomicLong(VersionConstants.UNINITIALIZED_VERSION);
  /** */
  private volatile long timestamp = System.currentTimeMillis();
  /** */
  private volatile int registerCount = 0;
  /** */
  private volatile String requestId = UUID.randomUUID().toString();

  private ReadWriteLock lock = new ReentrantReadWriteLock();

  /** The Read lock. */
  protected Lock readLock = lock.readLock();

  /** The Write lock. */
  protected Lock writeLock = lock.writeLock();

  /**
   * Assembly object.
   *
   * @return the object
   */
  public abstract Object assembly();

  /**
   * Is registered boolean.
   *
   * @return boolean boolean
   */
  @Override
  public boolean isRegistered() {
    readLock.lock();
    try {
      return registered;
    } finally {
      readLock.unlock();
    }
  }

  /** Wait to sync. */
  void waitToSync() {
    writeLock.lock();
    try {
      this.registered = false;
      this.requestId = UUID.randomUUID().toString();
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Sync ok.
   *
   * @param requestId the request id
   * @param version the version
   * @param refused the refused
   * @return the boolean
   */
  public boolean syncOK(String requestId, long version, boolean refused) {
    writeLock.lock();
    try {
      if (this.requestId.equals(requestId)) {
        this.registered = true;
        this.refused = refused;
        this.setAckVersion(version);
        return true;
      }
      return false;
    } finally {
      writeLock.unlock();
    }
  }

  /** @see Register#isEnabled() */
  @Override
  public boolean isEnabled() {
    readLock.lock();
    try {
      return enabled;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Sets enabled.
   *
   * @param requestId the request id
   */
  public void refused(String requestId) {
    writeLock.lock();
    try {
      if (this.requestId.equals(requestId)) {
        this.enabled = false;
        this.refused = true;
      }
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Is done boolean.
   *
   * @return boolean boolean
   */
  public boolean isDone() {
    readLock.lock();
    try {
      return (this.isRegistered() && this.pubVersion.get() == this.ackVersion.get())
          || this.isRefused();
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Assembly sync task sync task.
   *
   * @return the sync task
   */
  public SyncTask assemblySyncTask() {
    readLock.lock();
    try {
      SyncTask syncTask = new SyncTask();
      syncTask.setRequestId(requestId);
      syncTask.setRequest(assembly());
      syncTask.setDone(isDone());
      return syncTask;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Gets pub version.
   *
   * @return AtomicLong pub version
   */
  public AtomicLong getPubVersion() {
    return this.pubVersion;
  }

  /**
   * Sets ack version.
   *
   * @param version the ack version
   */
  public void setAckVersion(Long version) {
    if (null == version) {
      return;
    }

    long current = ackVersion.get();
    if (version <= current) {
      return;
    }

    boolean result = this.ackVersion.compareAndSet(current, version);
    if (result) {
      return;
    }

    setAckVersion(version);
  }

  /** @see Register#reset() */
  @Override
  public void reset() {
    writeLock.lock();
    try {
      this.registered = false;
      this.registerCount = 0;
      this.timestamp = System.currentTimeMillis();
      this.ackVersion = new AtomicLong(initialVersion.longValue());
      this.requestId = UUID.randomUUID().toString();
    } finally {
      writeLock.unlock();
    }
  }

  /** @see Register#unregister() */
  @Override
  public void unregister() {
    writeLock.lock();
    try {
      this.enabled = false;
      this.pubVersion.incrementAndGet();
      this.requestId = UUID.randomUUID().toString();
      this.registerCount = 0;
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Getter method for property <tt>refused</tt>.
   *
   * @return property value of refused
   */
  boolean isRefused() {
    readLock.lock();
    try {
      return refused;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Getter method for property <tt>timestamp</tt>.
   *
   * @return property value of timestamp
   */
  @Override
  public long getTimestamp() {
    readLock.lock();
    try {
      return timestamp;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Setter method for property <tt>timestamp</tt>.
   *
   * @param timestamp value to be assigned to property timestamp
   */
  void setTimestamp(long timestamp) {
    writeLock.lock();
    try {
      this.timestamp = timestamp;
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Sets auth signature.
   *
   * @param register the register
   */
  void setAuthSignature(BaseRegister register) {
    // auth signature
    if (null != authManager) {
      Map<String, String> authAttributes = authManager.getAuthContent(register);

      // merge auth attributes with exists register attributes
      Map<String, String> registerAttributes = register.getAttributes();
      if (null == registerAttributes) {
        registerAttributes = new HashMap<String, String>();
      }
      registerAttributes.putAll(authAttributes);
      register.setAttributes(registerAttributes);
    }
  }

  /**
   * Setter method for property <tt>authManager</tt>.
   *
   * @param authManager value to be assigned to property authManager
   */
  public void setAuthManager(AuthManager authManager) {
    this.authManager = authManager;
  }

  protected void setAttributes(
      BaseRegister to, BaseRegistration from, RegistryClientConfig config) {
    if (StringUtils.isNotEmpty(from.getInstanceId())) {
      to.setInstanceId(from.getInstanceId());
    } else {
      to.setInstanceId(config.getInstanceId());
    }
    if (StringUtils.isNotEmpty(config.getZone())) {
      to.setZone(config.getZone());
    } else {
      to.setZone(ValueConstants.DEFAULT_ZONE);
    }
    if (StringUtils.isNotEmpty(from.getAppName())) {
      to.setAppName(from.getAppName());
    } else {
      to.setAppName(config.getAppName());
    }
    if (StringUtils.isNotEmpty(from.getIp())) {
      to.setIp(from.getIp());
    } else {
      to.setIp(config.getIp());
    }
    to.setDataId(from.getDataId());
    to.setGroup(from.getGroup());
    to.setVersion(this.getPubVersion().get());
    to.setTimestamp(this.getTimestamp());
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "AbstractInternalRegister{"
        + "initialVersion="
        + initialVersion
        + ", registered="
        + registered
        + ", enabled="
        + enabled
        + ", refused="
        + refused
        + ", pubVersion="
        + pubVersion
        + ", ackVersion="
        + ackVersion
        + ", timestamp="
        + timestamp
        + ", registerCount="
        + registerCount
        + ", requestId='"
        + requestId
        + '}';
  }

  /** The type Sync task. */
  public static class SyncTask {

    private String requestId;

    private Object request;

    private boolean done;

    /**
     * Getter method for property <tt>requestId</tt>.
     *
     * @return property value of requestId
     */
    public String getRequestId() {
      return requestId;
    }

    /**
     * Setter method for property <tt>requestId</tt>.
     *
     * @param requestId value to be assigned to property requestId
     */
    void setRequestId(String requestId) {
      this.requestId = requestId;
    }

    /**
     * Getter method for property <tt>request</tt>.
     *
     * @return property value of request
     */
    public Object getRequest() {
      return request;
    }

    /**
     * Setter method for property <tt>request</tt>.
     *
     * @param request value to be assigned to property request
     */
    void setRequest(Object request) {
      this.request = request;
    }

    /**
     * Getter method for property <tt>done</tt>.
     *
     * @return property value of done
     */
    public boolean isDone() {
      return done;
    }

    /**
     * Setter method for property <tt>done</tt>.
     *
     * @param done value to be assigned to property done
     */
    public void setDone(boolean done) {
      this.done = done;
    }
  }
}
