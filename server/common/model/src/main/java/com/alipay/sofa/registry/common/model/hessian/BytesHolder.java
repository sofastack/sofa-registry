package com.alipay.sofa.registry.common.model.hessian;

/**
 * @author huicha
 * @date 2025/10/14
 */
public final class BytesHolder {

  public static BytesHolder of(byte[] bytes) {
    return new BytesHolder(bytes);
  }

  private final byte[] bytes;

  public BytesHolder(byte[] bytes) {
    this.bytes = bytes;
  }

  public byte[] getBytes() {
    return bytes;
  }

}
