package com.alipay.sofa.registry.common.model.sessionserver;

import java.io.Serializable;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerResp.java, v 0.1 2021年07月31日 16:28 xiaojian.xj Exp $
 */
public class ClientManagerResp implements Serializable {
  final boolean success;

  final Set<String> ips;

  public ClientManagerResp(boolean success) {
    this(success, null);
  }

  public ClientManagerResp(boolean success, Set<String> ips) {
    this.success = success;
    this.ips = ips;
  }

  public boolean isSuccess() {
    return success;
  }

  /**
   * Getter method for property <tt>ips</tt>.
   *
   * @return property value of ips
   */
  public Set<String> getIps() {
    return ips;
  }
}
