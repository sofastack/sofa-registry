package com.alipay.sofa.registry.server.session.push;

/**
 * @author huicha
 * @date 2025/9/8
 */
public class ChangeDebouncingTime {

  private int changeDebouncingMillis;

  private int changeDebouncingMaxMillis;

  public ChangeDebouncingTime() {
  }

  public ChangeDebouncingTime(int changeDebouncingMillis, int changeDebouncingMaxMillis) {
    this.changeDebouncingMillis = changeDebouncingMillis;
    this.changeDebouncingMaxMillis = changeDebouncingMaxMillis;
  }

  public int getChangeDebouncingMillis() {
    return changeDebouncingMillis;
  }

  public void setChangeDebouncingMillis(int changeDebouncingMillis) {
    this.changeDebouncingMillis = changeDebouncingMillis;
  }

  public int getChangeDebouncingMaxMillis() {
    return changeDebouncingMaxMillis;
  }

  public void setChangeDebouncingMaxMillis(int changeDebouncingMaxMillis) {
    this.changeDebouncingMaxMillis = changeDebouncingMaxMillis;
  }

}
