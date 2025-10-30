package com.alipay.sofa.registry.server.session.push;

import java.util.Objects;

/**
 * @author huicha
 * @date 2025/10/27
 */
public interface ChangeTask<Key> extends Comparable<ChangeTask<Key>> {

  Key key();

  long deadline();

  @Override
  default int compareTo(ChangeTask<Key> o) {
    Key key = this.key();
    Key otherKey = o.key();

    if (Objects.equals(key, otherKey)) {
      return 0;
    }

    return Long.compare(this.deadline(), o.deadline());
  }

}
