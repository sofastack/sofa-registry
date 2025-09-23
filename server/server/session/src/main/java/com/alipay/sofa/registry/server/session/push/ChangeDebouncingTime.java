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

/**
 * @author huicha
 * @date 2025/9/8
 */
public class ChangeDebouncingTime {

  private int changeDebouncingMillis;

  private int changeDebouncingMaxMillis;

  /**
 * Creates a ChangeDebouncingTime with default values (both debouncing fields set to 0).
 */
public ChangeDebouncingTime() {}

  /**
   * Create a ChangeDebouncingTime with specified debouncing durations.
   *
   * @param changeDebouncingMillis the base debouncing duration in milliseconds
   * @param changeDebouncingMaxMillis the maximum allowed debouncing duration in milliseconds (upper bound for debouncing)
   */
  public ChangeDebouncingTime(int changeDebouncingMillis, int changeDebouncingMaxMillis) {
    this.changeDebouncingMillis = changeDebouncingMillis;
    this.changeDebouncingMaxMillis = changeDebouncingMaxMillis;
  }

  /**
   * Returns the configured debounce interval for a change, in milliseconds.
   *
   * @return the change debouncing interval in milliseconds
   */
  public int getChangeDebouncingMillis() {
    return changeDebouncingMillis;
  }

  /**
   * Set the debouncing duration for a single change, in milliseconds.
   *
   * @param changeDebouncingMillis the debouncing time in milliseconds
   */
  public void setChangeDebouncingMillis(int changeDebouncingMillis) {
    this.changeDebouncingMillis = changeDebouncingMillis;
  }

  /**
   * Returns the maximum allowed debouncing delay for changes, in milliseconds.
   *
   * @return the maximum change debouncing time in milliseconds
   */
  public int getChangeDebouncingMaxMillis() {
    return changeDebouncingMaxMillis;
  }

  /**
   * Sets the maximum debouncing interval for change notifications, in milliseconds.
   *
   * @param changeDebouncingMaxMillis maximum debounce duration in milliseconds
   */
  public void setChangeDebouncingMaxMillis(int changeDebouncingMaxMillis) {
    this.changeDebouncingMaxMillis = changeDebouncingMaxMillis;
  }
}
